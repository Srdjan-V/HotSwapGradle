package io.github.srdjanv.hotswapgradle.registry.internal;

import io.github.srdjanv.hotswapgradle.HotswapGradleService;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.registry.IKnownDcevmRegistry;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataResolver;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmSpecResolver;
import io.github.srdjanv.hotswapgradle.resolver.ILauncherResolver;
import io.github.srdjanv.hotswapgradle.suppliers.KnownDcevmSupplier;
import io.github.srdjanv.hotswapgradle.util.JavaUtil;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.Action;
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
            IDcevmMetadataResolver metadataResolver,
            ILauncherResolver launcherResolver,
            IDcevmSpecResolver specResolver,
            final DcevmSpec dcevmSpec) {
        JavaLauncher javaLauncher = null;
        lock.lock();
        try {
            if (!dcevmSpec.getQueryKnownDEVMs().get()) {
                logger.info("Skipping query of {}, in KnownRegistry", dcevmSpec);
                return null;
            }
            JavaVersion javaVersion = JavaUtil.versionOf(dcevmSpec);
            List<Action<? super DcevmSpec>> specs = dcevmRegistry.get(javaVersion);
            if (specs == null || specs.isEmpty()) {
                logger.info(
                        "Skipping KnownRegistry query, no DcevmSpec for java version {}. Requested DcevmSpec {}",
                        javaVersion,
                        dcevmSpec);
                return null;
            }

            Map<Preference, List<Pair<Action<? super DcevmSpec>, DcevmSpec>>> resolvedSpecs = new HashMap<>();
            for (Action<? super DcevmSpec> spec : specs) {
                var knownSpec = specResolver.resolveDcevmSpec(service, spec);
                var preference = Preference.getPreference(knownSpec, dcevmSpec);
                resolvedSpecs
                        .computeIfAbsent(preference, k -> new ArrayList<>())
                        .add(Pair.of(spec, knownSpec));
            }

            topBreak:
            for (Preference value : Preference.values()) {
                List<Pair<Action<? super DcevmSpec>, DcevmSpec>> specPairs = resolvedSpecs.get(value);
                if (specPairs == null) continue;
                for (Pair<Action<? super DcevmSpec>, DcevmSpec> specPair : specPairs) {
                    var resolvedLauncher = launcherResolver.resolveLauncher(specPair.getRight());
                    try {
                        javaLauncher = resolvedLauncher.get();
                        var metadata = metadataResolver.resolveDcevmMetadata(javaLauncher
                                .getMetadata()
                                .getInstallationPath()
                                .getAsFile()
                                .toPath());
                        if (!metadata.getIsDcevmPresent().get()) {
                            logger.info("Resolved known spec is not an DCEVM, known spec {}", specPair.getRight());
                            javaLauncher = null;
                            continue;
                        }
                        dcevmSpec.getDcevmMetadata().set(metadata);
                        specPair.getLeft().execute(dcevmSpec);
                        break topBreak;
                    } catch (Exception e) {
                        logger.info("Failed to resolve DCEVM spec {} in KnownRegistry", dcevmSpec, e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return javaLauncher;
    }

    private enum Preference implements BiPredicate<DcevmSpec, DcevmSpec> {
        VENDOR((knownSpec, requestedSpec) -> {
            if (knownSpec.getVendor().isPresent() && requestedSpec.getVendor().isPresent())
                return knownSpec
                        .getVendor()
                        .get()
                        .matches(requestedSpec.getVendor().get().toString());
            return false;
        }),
        ANYTHING((knownSpeck, requestedSpec) -> true);

        private final BiPredicate<DcevmSpec, DcevmSpec> predicate;

        Preference(BiPredicate<DcevmSpec, DcevmSpec> predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean test(DcevmSpec knownSpec, DcevmSpec requestedSpec) {
            return predicate.test(knownSpec, requestedSpec);
        }

        static Preference getPreference(DcevmSpec knownSpeck, DcevmSpec requestedSpec) {
            for (Preference value : Preference.values()) {
                if (value.test(knownSpeck, requestedSpec)) return value;
            }
            throw new IllegalStateException("Unknown preference: " + knownSpeck);
        }
    }
}
