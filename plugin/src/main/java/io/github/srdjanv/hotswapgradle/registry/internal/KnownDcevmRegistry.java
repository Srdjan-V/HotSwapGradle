package io.github.srdjanv.hotswapgradle.registry.internal;

import io.github.srdjanv.hotswapgradle.HotswapGradleService;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.registry.IKnownDcevmRegistry;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmSpecResolver;
import io.github.srdjanv.hotswapgradle.resolver.ILauncherResolver;
import io.github.srdjanv.hotswapgradle.suppliers.KnownDcevmSupplier;
import io.github.srdjanv.hotswapgradle.util.JavaUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.jetbrains.annotations.Nullable;

public class KnownDcevmRegistry implements IKnownDcevmRegistry {
    private final Lock lock = new ReentrantLock();
    private final HotswapGradleService service;
    private final Map<JavaVersion, List<Action<? super DcevmSpec>>> dcevmRegistry = new HashMap<>();

    public KnownDcevmRegistry(HotswapGradleService service) {
        this.service = service;
        setDefaultRegistry();
    }

    @Override
    public void setDefaultRegistry() {
        try {
            lock.lock();
            IKnownDcevmRegistry.super.setDefaultRegistry();
        } finally {
            lock.unlock();
        }
    }

    public void populateRegistry(JavaVersion javaVersion, KnownDcevmSupplier knownDCEVMSupplier) {
        lock.lock();
        try {
            List<Action<? super DcevmSpec>> dcvmList =
                    dcevmRegistry.computeIfAbsent(javaVersion, k -> new ArrayList<>());
            dcvmList.addAll(knownDCEVMSupplier.getKnownDCEVMs());
        } finally {
            lock.unlock();
        }
    }

    public void configureRegistry(Consumer<Map<JavaVersion, List<Action<? super DcevmSpec>>>> configureRegistry) {
        lock.lock();
        try {
            configureRegistry.accept(dcevmRegistry);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public @Nullable JavaLauncher locateVM(
            ILauncherResolver launcherResolver, IDcevmSpecResolver specResolver, DcevmSpec dcevmSpec) {
        JavaLauncher javaLauncher = null;
        lock.lock();
        try {
            if (!dcevmSpec.getQueryKnownDEVMs().get()) return javaLauncher;
            JavaVersion javaVersion = JavaUtil.versionOf(dcevmSpec);
            var specs = dcevmRegistry.get(javaVersion);
            if (specs == null || specs.isEmpty()) return javaLauncher;
            for (Action<? super DcevmSpec> spec : specs) {
                var resolvedSpec = specResolver.resolveDcevmSpec(service, spec);
                var resolvedLauncher = launcherResolver.resolveLauncher(resolvedSpec);
                try {
                    javaLauncher = resolvedLauncher.get();
                    spec.execute(dcevmSpec);
                    break;
                } catch (GradleException ignore) {
                }
            }
        } finally {
            lock.unlock();
        }
        return javaLauncher;
    }
}
