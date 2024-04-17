package io.github.srdjanv.hotswapgradle.registry.internal;

import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.suppliers.CachedDcevmSupplier;
import io.github.srdjanv.hotswapgradle.registry.ICashedJVMRegistry;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataLauncherResolver;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataResolver;
import io.github.srdjanv.hotswapgradle.util.FileUtils;
import io.github.srdjanv.hotswapgradle.util.JavaUtil;
import io.github.srdjanv.hotswapgradle.validator.DcevmValidator;
import org.gradle.api.JavaVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CashedJVMRegistry implements ICashedJVMRegistry {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
    private final Lock lock = new ReentrantLock();
    public final File registryPath;
    private DcevmValidator validator;
    private final Map<Path, DcevmMetadata> dcevmMetadataCache = new HashMap<>();
    private final Supplier<Map<JavaVersion, List<Path>>> dcevmRegistry;
    private boolean regDirty = false;
    private boolean initialized = false;
    private boolean modified = false;

    public CashedJVMRegistry(DcevmValidator validator, File registryPath) {
        this.registryPath = registryPath;
        this.validator = validator;
        dcevmRegistry = Suppliers.memoize(this::initRegistry);
    }

    @Override public void setValidator(@NotNull DcevmValidator validator) {
        this.validator = Objects.requireNonNull(validator);
    }

    private Map<JavaVersion, List<Path>> initRegistry() {
        lock.lock();
        Map<JavaVersion, List<Path>> resolvedDcevmRegistry = new HashMap<>();
        try {
            String text = FileUtils.loadTextFromFile(registryPath);
            Map<JavaVersion, List<File>> dcevmRegistry = null;
            try {
                dcevmRegistry =  gson.fromJson(text, new TypeToken<Map<JavaVersion, File>>() {}.getType());
            } catch (JsonParseException ignore) {}

            if (dcevmRegistry != null) {
                for (Map.Entry<JavaVersion, List<File>> entry : dcevmRegistry.entrySet()) {
                    if (entry.getValue() == null || entry.getValue().isEmpty()) continue;
                    JavaVersion javaVersion = entry.getKey();
                    List<Path> dcevmPaths = entry.getValue()
                            .stream()
                            .map(File::toPath)
                            .filter(validator::validateDcevm)
                            .collect(Collectors.toList());
                    resolvedDcevmRegistry.put(javaVersion, dcevmPaths);
                }
                initialized = true;
            }
        } finally {
            lock.unlock();
        }
        return resolvedDcevmRegistry;
    }

    @Override
    public void saveRegistry() {
        lock.lock();
        try {
            if (!regDirty && !initialized && !modified) return;
            Map<JavaVersion, List<File>> data = new HashMap<>();

            if (initialized) {
                for (Map.Entry<JavaVersion, List<Path>> entry : dcevmRegistry.get().entrySet()) {
                    List<File> paths = entry
                            .getValue()
                            .stream()
                            .map(path -> path.normalize().toAbsolutePath().toFile())
                            .collect(Collectors.toList());
                    data.put(entry.getKey(), paths);
                }
            }

            if (modified) {
                Map<JavaVersion, List<Path>> metaData = dcevmMetadataCache.values()
                        .stream()
                        .collect(Collectors.groupingBy(metadata -> JavaUtil.versionOf(metadata.getJavaInstallationMetadata().get().getLanguageVersion()),
                                Collectors.mapping(metadata -> metadata.getJavaInstallationMetadata().get().getInstallationPath().getAsFile().toPath(),
                                        Collectors.toList())
                        ));
                for (Map.Entry<JavaVersion, List<Path>> entry : metaData.entrySet()) {
                    List<File> normalizedPaths = entry
                            .getValue()
                            .stream()
                            .map(path -> path.normalize().toAbsolutePath().toFile())
                            .collect(Collectors.toList());

                    data.merge(entry.getKey(), normalizedPaths, (path1, path2) -> {
                        List<File> dataList = new ArrayList<>();
                        dataList.addAll(path1);
                        dataList.addAll(path2);
                        return dataList;
                    });
                }
            }

            if (data.isEmpty()) {
                regDirty = false;
                return;
            }
            FileUtils.saveTestToFile(gson.toJson(data), registryPath);
            regDirty = false;
        } finally {
            lock.unlock();
        }
    }

    @Override public void populateRegistry(
            JavaVersion javaVersion,
            CachedDcevmSupplier cachedDcevmSupplier) {
        lock.lock();
        try {
            List<Path> valid = new ArrayList<>();
            for (Path localJvm : cachedDcevmSupplier.getLocalJvms()) {
                if (validator.validateDcevm(localJvm)) valid.add(localJvm);
            }

            if (!valid.isEmpty()) {
                regDirty = true;
                var reg = dcevmRegistry.get();
                var paths = reg.computeIfAbsent(javaVersion, k -> new ArrayList<>());
                paths.addAll(valid);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void addToRegistry(DcevmMetadata metadata) {
        lock.lock();
        try {
            var javaMetadata = metadata.getJavaInstallationMetadata().get();
            var javaVersion = JavaUtil.versionOf(javaMetadata.getLanguageVersion());
            var javaHome = javaMetadata.getInstallationPath().getAsFile().toPath();
            dcevmMetadataCache.put(javaHome, metadata);

            var reg = dcevmRegistry.get();
            var paths = reg.computeIfAbsent(javaVersion, k -> new ArrayList<>());
            paths.add(javaHome);
            modified = true;
            regDirty = true;
        } finally {
            lock.unlock();
        }
    }

    @Override public @Nullable JavaLauncher locateVM(
            IDcevmMetadataResolver metadataResolver,
            IDcevmMetadataLauncherResolver metadataLauncherResolver,
            DcevmSpec dcevmSpec) {
        JavaLauncher javaLauncher = null;
        lock.lock();
        try {
            if (!dcevmSpec.getQueryCachedDEVMs().get()) return javaLauncher;
            JavaVersion javaVersion = JavaUtil.versionOf(dcevmSpec);

            var paths = dcevmRegistry.get().get(javaVersion);
            if (paths == null || paths.isEmpty()) return javaLauncher;
            for (var path : paths) {
                var iDcevmMetadata = dcevmMetadataCache.get(path);
                if (iDcevmMetadata == null) {
                    iDcevmMetadata = metadataResolver.resolveDcevmMetadata(path);
                    if (iDcevmMetadata != null) addToRegistry(iDcevmMetadata);
                }
                if (iDcevmMetadata != null) dcevmSpec.getDcevmMetadata().set(iDcevmMetadata);
                var resolvedLauncher = metadataLauncherResolver.resolveLauncher(iDcevmMetadata);
                if (resolvedLauncher == null) return javaLauncher;
                javaLauncher = resolvedLauncher.get();
            }
        } finally {
            lock.unlock();
        }
        return javaLauncher;
    }
}
