package io.github.srdjanv.hotswapgradle.registry.internal;

import io.github.srdjanv.hotswapgradle.HotswapGradleService;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.registry.ICashedJVMRegistry;
import io.github.srdjanv.hotswapgradle.registry.ILocalJVMRegistry;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataLauncherResolver;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataResolver;
import io.github.srdjanv.hotswapgradle.resolver.IJVMResolver;
import io.github.srdjanv.hotswapgradle.resolver.ILauncherResolver;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.jetbrains.annotations.Nullable;

public class LocalJVMRegistry implements ILocalJVMRegistry {
    private final Lock lock = new ReentrantLock();
    private final ICashedJVMRegistry cashedJVMRegistry;

    public LocalJVMRegistry(HotswapGradleService service) {
        this.cashedJVMRegistry = service.getCashedJVMRegistry();
    }

    private void addToCachedRegistry(List<DcevmMetadata> metadataList) {
        for (DcevmMetadata metadata : metadataList) {
            cashedJVMRegistry.addToRegistry(metadata);
        }
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
            if (!dcevmSpec.getQueryLocalDEVMs().get()) return javaLauncher;
            var allLocalDcevms = jvmResolver.getAllDcevmToolchains(metadataResolver);
            addToCachedRegistry(allLocalDcevms);

            Provider<JavaLauncher> javaLauncherProvider;
            for (DcevmMetadata metadata : allLocalDcevms) {
                javaLauncherProvider = metadataLauncherResolver.resolveLauncher(metadata);
                if (javaLauncherProvider != null) {
                    javaLauncher = javaLauncherProvider.get();
                    dcevmSpec.getDcevmMetadata().set(metadata);
                    break;
                }
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
