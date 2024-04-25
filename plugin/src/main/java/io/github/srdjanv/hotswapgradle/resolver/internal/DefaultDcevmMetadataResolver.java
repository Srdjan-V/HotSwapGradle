package io.github.srdjanv.hotswapgradle.resolver.internal;

import io.github.srdjanv.hotswapgradle.HotswapGradleService;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.dcvm.internal.DefaultDcevmMetadata;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataResolver;
import io.github.srdjanv.hotswapgradle.util.JavaUtil;
import java.nio.file.Path;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.internal.jvm.inspection.JvmInstallationMetadata;
import org.gradle.internal.jvm.inspection.JvmMetadataDetector;
import org.gradle.internal.jvm.inspection.JvmToolchainMetadata;
import org.gradle.jvm.toolchain.internal.InstallationLocation;

public class DefaultDcevmMetadataResolver implements IDcevmMetadataResolver {
    private final HotswapGradleService service;
    private final JvmMetadataDetector metadataDetector;
    private final ObjectFactory objectFactory;

    @Inject
    public DefaultDcevmMetadataResolver(
            Project project, ObjectFactory objectFactory, JvmMetadataDetector metadataDetector) {
        service =
                JavaUtil.getHotswapExtension(project).getHotswapGradleService().get();
        this.metadataDetector = metadataDetector;
        this.objectFactory = objectFactory;
    }

    @Override
    public DcevmMetadata resolveDcevmMetadata(Path javaHome) {
        InstallationLocation installationLocation =
                new InstallationLocation(javaHome.toFile(), "DefaultDcevmMetadataResolver");
        JvmInstallationMetadata installationMetadata = metadataDetector.getMetadata(installationLocation);
        JvmToolchainMetadata jvmToolchainMetadata =
                new JvmToolchainMetadata(installationMetadata, installationLocation);
        return objectFactory.newInstance(DefaultDcevmMetadata.class, jvmToolchainMetadata, service);
    }
}
