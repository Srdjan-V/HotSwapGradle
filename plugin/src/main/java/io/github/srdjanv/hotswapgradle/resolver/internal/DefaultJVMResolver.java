package io.github.srdjanv.hotswapgradle.resolver.internal;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataResolver;
import io.github.srdjanv.hotswapgradle.resolver.IJVMResolver;
import io.github.srdjanv.hotswapgradle.util.DCEVMUtil;
import org.gradle.internal.jvm.inspection.JavaInstallationRegistry;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultJVMResolver implements IJVMResolver {
    private final JavaInstallationRegistry registry;

    @Inject
    public DefaultJVMResolver(JavaInstallationRegistry registry) {
        this.registry = registry;
    }

    @Override public List<DcevmMetadata> getAllDcevmToolchains(IDcevmMetadataResolver metadataResolver) {
        return registry.toolchains()
                .stream()
                .filter(tool -> tool.metadata.isValidInstallation())
                .filter(tool -> DCEVMUtil.isDCEVMPresent(tool.metadata.getJavaHome()))
                .map(tool -> metadataResolver.resolveDcevmMetadata(tool.metadata.getJavaHome()))
                .collect(Collectors.toList());
    }

}
