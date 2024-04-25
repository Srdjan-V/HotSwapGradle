package io.github.srdjanv.hotswapgradle.resolver.internal;

import io.github.srdjanv.hotswapgradle.HotswapGradleService;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataResolver;
import io.github.srdjanv.hotswapgradle.resolver.IJVMResolver;
import io.github.srdjanv.hotswapgradle.util.JavaUtil;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.internal.jvm.inspection.JavaInstallationRegistry;

public class DefaultJVMResolver implements IJVMResolver {
    private final JavaInstallationRegistry registry;
    private final HotswapGradleService service;

    @Inject
    public DefaultJVMResolver(Project project, JavaInstallationRegistry registry) {
        service =
                JavaUtil.getHotswapExtension(project).getHotswapGradleService().get();
        this.registry = registry;
    }

    @Override
    public List<DcevmMetadata> getAllDcevmToolchains(IDcevmMetadataResolver metadataResolver) {
        return registry.toolchains().stream()
                .filter(tool -> tool.metadata.isValidInstallation())
                .filter(tool -> service.getDcevmValidator().validateDcevm(tool.metadata.getJavaHome()))
                .map(tool -> metadataResolver.resolveDcevmMetadata(tool.metadata.getJavaHome()))
                .collect(Collectors.toList());
    }
}
