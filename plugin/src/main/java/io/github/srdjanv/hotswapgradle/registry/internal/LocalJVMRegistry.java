package io.github.srdjanv.hotswapgradle.registry.internal;

import io.github.srdjanv.hotswapgradle.HotswapGradleService;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.registry.ICachedJVMRegistry;
import io.github.srdjanv.hotswapgradle.registry.ILocalJVMRegistry;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataLauncherResolver;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataResolver;
import io.github.srdjanv.hotswapgradle.resolver.IJVMResolver;
import io.github.srdjanv.hotswapgradle.resolver.ILauncherResolver;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalJVMRegistry implements ILocalJVMRegistry {
    private final Logger logger = LoggerFactory.getLogger(LocalJVMRegistry.class);
    private final Lock lock = new ReentrantLock();
    private final ICachedJVMRegistry cashedJVMRegistry;

    public LocalJVMRegistry(HotswapGradleService service) {
        this.cashedJVMRegistry = service.getCashedJVMRegistry();
    }

    @Override
    public @Nullable JavaLauncher locateVM(
            IJVMResolver jvmResolver,
            IDcevmMetadataResolver metadataResolver,
            IDcevmMetadataLauncherResolver metadataLauncherResolver,
            DcevmSpec dcevmSpec) {
        lock.lock();
        JavaLauncher javaLauncher = null;
        try {
            if (!dcevmSpec.getQueryLocalDEVMs().get()) {
                logger.info("Skipping query of {}, in LocalJVMRegistry", dcevmSpec);
                return null;
            }
            try {
                var allLocalDcevms = jvmResolver.getAllDcevmToolchains(metadataResolver);

                // addToCachedRegistry
                for (DcevmMetadata metadata : allLocalDcevms) {
                    cashedJVMRegistry.addToRegistry(metadata);
                }

                Provider<JavaLauncher> javaLauncherProvider;
                for (DcevmMetadata metadata : allLocalDcevms) {
                    if (!metadata.getJavaInstallationMetadata()
                            .get()
                            .getLanguageVersion()
                            .equals(dcevmSpec.getLanguageVersion().get())) continue;

                    javaLauncherProvider = metadataLauncherResolver.resolveLauncher(metadata);
                    if (javaLauncherProvider != null) {
                        javaLauncher = javaLauncherProvider.get();
                        dcevmSpec.getDcevmMetadata().set(metadata);
                        break;
                    }
                }
            } catch (Exception e) {
                logger.info("Unable query of {}, in LocalJVMRegistry", dcevmSpec, e);
            }

            return javaLauncher;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public JavaLauncher locateVanillaVM(ILauncherResolver launcherResolver, DcevmSpec dcevmSpec) {
        lock.lock();
        try {
            return launcherResolver
                    .resolveLauncher(dcevmSpec.getFallbackSpeck().get())
                    .get();
        } finally {
            lock.unlock();
        }
    }
}
