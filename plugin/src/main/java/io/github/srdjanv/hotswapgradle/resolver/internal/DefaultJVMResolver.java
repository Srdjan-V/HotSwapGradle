package io.github.srdjanv.hotswapgradle.resolver.internal;

import io.github.srdjanv.hotswapgradle.dcevmdetection.legacy.LegacyDcevmDetection;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataResolver;
import io.github.srdjanv.hotswapgradle.resolver.IJVMResolver;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.gradle.internal.jvm.inspection.JavaInstallationRegistry;

public class DefaultJVMResolver implements IJVMResolver {
    private final JavaInstallationRegistry registry;

    @Inject
    public DefaultJVMResolver(JavaInstallationRegistry registry) {
        this.registry = registry;
    }

    @Override
    public List<DcevmMetadata> getAllDcevmToolchains(IDcevmMetadataResolver metadataResolver) {
        return registry.toolchains().stream()
                .filter(tool -> tool.metadata.isValidInstallation())
                .filter(tool -> LegacyDcevmDetection.isPresent(tool.metadata.getJavaHome()))
                .map(tool -> metadataResolver.resolveDcevmMetadata(tool.metadata.getJavaHome()))
                .collect(Collectors.toList());
    }
}
