package io.github.srdjanv.hotswapgradle.registry.internal;

import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.github.srdjanv.hotswapgradle.HotswapGradleService;
import io.github.srdjanv.hotswapgradle.agent.HotswapAgentProvider;
import io.github.srdjanv.hotswapgradle.dcevmdetection.legacy.LegacyDcevmDetection;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.registry.ICashedJVMRegistry;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataLauncherResolver;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataResolver;
import io.github.srdjanv.hotswapgradle.suppliers.CachedDcevmSupplier;
import io.github.srdjanv.hotswapgradle.util.FileIOUtils;
import io.github.srdjanv.hotswapgradle.util.FileUtils;
import io.github.srdjanv.hotswapgradle.util.JavaUtil;
import io.github.srdjanv.hotswapgradle.validator.DcevmValidator;
import java.io.File;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.gradle.api.JavaVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CashedJVMRegistry implements ICashedJVMRegistry {
    private final Logger logger = LoggerFactory.getLogger(HotswapAgentProvider.class);
    private final Lock lock = new ReentrantLock();
    private final HotswapGradleService service;
    private final Gson gson;
    public final File registryPath;
    private DcevmValidator validator;
    private final Map<Path, DcevmMetadata> dcevmMetadataCache = new HashMap<>();
    private final Supplier<Map<JavaVersion, List<Path>>> dcevmRegistry;
    private boolean regDirty = false;
    private boolean initialized = false;
    private boolean modified = false;

    public CashedJVMRegistry(HotswapGradleService service) {
        this.service = service;

        var gsonBuilder = new GsonBuilder().enableComplexMapKeySerialization();
        if (service.getParameters().getDebug().get()) gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();

        this.registryPath =
                FileUtils.jdkData(service.getParameters().getWorkingDirectory()).getAsFile();
        this.validator = LegacyDcevmDetection::isPresent;
        dcevmRegistry = Suppliers.memoize(this::initRegistry);
    }

    @Override
    public void setValidator(@NotNull DcevmValidator validator) {
        this.validator = Objects.requireNonNull(validator);
    }

    private Map<JavaVersion, List<Path>> initRegistry() {
        lock.lock();
        Map<JavaVersion, List<Path>> resolvedDcevmRegistry = new HashMap<>();
        if (!service.getParameters().getIsCachedRegistryPersistent().get()) return resolvedDcevmRegistry;
        try {
            Map<JavaVersion, Set<String>> dcevmRegistry = null;
            String text = null;
            try {
                text = FileIOUtils.loadTextFromFile(registryPath);
            } catch (UncheckedIOException e) {
                logger.debug("Failed to load registry from {}", registryPath, e);
            }
            try {
                if (text != null)
                    dcevmRegistry = gson.fromJson(text, new TypeToken<Map<JavaVersion, Set<String>>>() {}.getType());
            } catch (JsonParseException exception) {
                logger.debug("Failed to parse dcevm registry", exception);
            }

            if (dcevmRegistry != null) {
                for (Map.Entry<JavaVersion, Set<String>> entry : dcevmRegistry.entrySet()) {
                    if (entry.getValue() == null || entry.getValue().isEmpty()) continue;
                    JavaVersion javaVersion = entry.getKey();
                    List<Path> dcevmPaths = entry.getValue().stream()
                            .map(File::new)
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
            if (!service.getParameters().getIsCachedRegistryPersistent().get()) return;
            if (!regDirty && !initialized && !modified) return;
            Map<JavaVersion, List<String>> data = new HashMap<>();

            if (initialized) {
                for (Map.Entry<JavaVersion, List<Path>> entry :
                        dcevmRegistry.get().entrySet()) {
                    List<String> paths = entry.getValue().stream()
                            .map(path -> path.normalize().toAbsolutePath())
                            .map(Path::toFile)
                            .map(File::toString)
                            .collect(Collectors.toList());
                    data.put(entry.getKey(), paths);
                }
            }

            if (modified) {
                Map<JavaVersion, List<Path>> metaData = dcevmMetadataCache.values().stream()
                        .collect(Collectors.groupingBy(
                                metadata -> JavaUtil.versionOf(metadata.getJavaInstallationMetadata()
                                        .get()
                                        .getLanguageVersion()),
                                Collectors.mapping(
                                        metadata -> metadata.getJavaInstallationMetadata()
                                                .get()
                                                .getInstallationPath()
                                                .getAsFile()
                                                .toPath(),
                                        Collectors.toList())));
                for (Map.Entry<JavaVersion, List<Path>> entry : metaData.entrySet()) {
                    List<String> normalizedPaths = entry.getValue().stream()
                            .map(path -> path.normalize().toAbsolutePath())
                            .map(Path::toFile)
                            .map(File::toString)
                            .collect(Collectors.toList());

                    data.merge(entry.getKey(), normalizedPaths, (path1, path2) -> {
                        List<String> dataList = new ArrayList<>();
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

            try {
                FileIOUtils.saveStringToFile(gson.toJson(data), registryPath);
            } catch (UncheckedIOException exception) {
                logger.debug("Failed to save registry, path {}", registryPath, exception);
            } catch (JsonParseException exception) {
                logger.debug("Failed to parse registry while saving", exception);
            }
            regDirty = false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void populateRegistry(JavaVersion javaVersion, CachedDcevmSupplier cachedDcevmSupplier) {
        lock.lock();
        try {
            Set<Path> valid = new HashSet<>();
            for (Path localJvm : cachedDcevmSupplier.getLocalJvms()) {
                if (validator.validateDcevm(localJvm)) {
                    if (valid.add(localJvm)) {
                        logger.info("Populating CashedJVMRegistry with local jvm {}", localJvm);
                    }
                } else logger.info("Invalid jvm {}", localJvm);
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
            logger.info("Adding DcevmMetadata to registry {}", metadata);
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

    @Override
    public @Nullable JavaLauncher locateVM(
            IDcevmMetadataResolver metadataResolver,
            IDcevmMetadataLauncherResolver metadataLauncherResolver,
            DcevmSpec dcevmSpec) {
        JavaLauncher javaLauncher = null;
        lock.lock();
        try {
            if (!dcevmSpec.getQueryCachedDEVMs().get()) {
                logger.debug("Skipping query of {}, in CachedRegistry", dcevmSpec);
                return null;
            }
            JavaVersion javaVersion = JavaUtil.versionOf(dcevmSpec);

            var paths = dcevmRegistry.get().get(javaVersion);
            if (paths == null || paths.isEmpty()) {
                logger.debug(
                        "Skipping CachedRegistry query, no paths for java version {}. Requested DcevmSpec {}",
                        javaVersion,
                        dcevmSpec);
                return null;
            }
            for (var path : paths) {
                var iDcevmMetadata = dcevmMetadataCache.get(path);
                if (iDcevmMetadata == null) {
                    iDcevmMetadata = metadataResolver.resolveDcevmMetadata(path);
                    if (iDcevmMetadata != null) addToRegistry(iDcevmMetadata);
                }
                if (iDcevmMetadata != null) dcevmSpec.getDcevmMetadata().set(iDcevmMetadata);
                var resolvedLauncher = metadataLauncherResolver.resolveLauncher(iDcevmMetadata);
                if (resolvedLauncher == null) {
                    logger.debug(
                            "Unable to resolve CachedRegistry launcher for {}, Requested DcevmSpec {}",
                            iDcevmMetadata,
                            dcevmSpec);
                    return null;
                }
                javaLauncher = resolvedLauncher.get();
            }
        } finally {
            lock.unlock();
        }
        return javaLauncher;
    }
}
