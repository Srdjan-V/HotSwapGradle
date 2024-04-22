package io.github.srdjanv.hotswapgradle.registry.internal;

import io.github.srdjanv.hotswapgradle.HotswapGradleService;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.registry.IKnownDcevmRegistry;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmSpecResolver;
import io.github.srdjanv.hotswapgradle.resolver.ILauncherResolver;
import io.github.srdjanv.hotswapgradle.suppliers.KnownDcevmSupplier;
import io.github.srdjanv.hotswapgradle.util.JavaUtil;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KnownDcevmRegistry implements IKnownDcevmRegistry {
    private final Logger logger = LoggerFactory.getLogger(KnownDcevmRegistry.class);
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
            ILauncherResolver launcherResolver, IDcevmSpecResolver specResolver, final DcevmSpec dcevmSpec) {
        JavaLauncher javaLauncher = null;
        lock.lock();
        try {
            if (!dcevmSpec.getQueryKnownDEVMs().get()) return javaLauncher;
            JavaVersion javaVersion = JavaUtil.versionOf(dcevmSpec);
            List<Action<? super DcevmSpec>> specs = dcevmRegistry.get(javaVersion);
            if (specs == null || specs.isEmpty()) return javaLauncher;

            Map<Preference, List<Pair<Action<? super DcevmSpec>, DcevmSpec>>> resolvedSpecs = new HashMap<>();
            for (Action<? super DcevmSpec> spec : specs) {
                var knownSpec = specResolver.resolveDcevmSpec(service, spec);
                var preference = Preference.getPreference(knownSpec, dcevmSpec);
                resolvedSpecs
                        .computeIfAbsent(preference, k -> new ArrayList<>())
                        .add(Pair.of(spec, knownSpec));
            }

            topBrake:
            for (Preference value : Preference.values()) {
                List<Pair<Action<? super DcevmSpec>, DcevmSpec>> specPairs = resolvedSpecs.get(value);
                if (specPairs == null) continue;
                for (Pair<Action<? super DcevmSpec>, DcevmSpec> specPair : specPairs) {
                    var resolvedLauncher = launcherResolver.resolveLauncher(specPair.getRight());
                    try {
                        javaLauncher = resolvedLauncher.get();
                        specPair.getLeft().execute(dcevmSpec);
                        break topBrake;
                    } catch (GradleException e) {
                        logger.debug("Failed to resolve DCEVM spec", e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return javaLauncher;
    }

    private enum Preference {
        VENDOR {
            @Override
            boolean matching(DcevmSpec knownSpeck, DcevmSpec requestedSpec) {
                if (knownSpeck.getVendor().isPresent()
                        && requestedSpec.getVendor().isPresent())
                    return knownSpeck
                            .getVendor()
                            .get()
                            .matches(requestedSpec.getVendor().get().toString());
                return false;
            }
        },
        ANYTHING {
            @Override
            boolean matching(DcevmSpec knownSpeck, DcevmSpec requestedSpec) {
                return true;
            }
        };

        static Preference getPreference(DcevmSpec knownSpeck, DcevmSpec requestedSpec) {
            for (Preference value : Preference.values()) {
                if (value.matching(knownSpeck, requestedSpec)) {
                    return value;
                }
            }
            throw new IllegalStateException("Unknown preference: " + knownSpeck);
        }

        abstract boolean matching(DcevmSpec knownSpeck, DcevmSpec requestedSpec);
    }
}
