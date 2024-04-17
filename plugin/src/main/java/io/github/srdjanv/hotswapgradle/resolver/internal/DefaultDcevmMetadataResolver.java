package io.github.srdjanv.hotswapgradle.resolver.internal;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.dcvm.internal.DefaultDcevmMetadata;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataResolver;
import org.gradle.api.model.ObjectFactory;
import org.gradle.internal.jvm.inspection.JvmInstallationMetadata;
import org.gradle.internal.jvm.inspection.JvmMetadataDetector;
import org.gradle.internal.jvm.inspection.JvmToolchainMetadata;
import org.gradle.jvm.toolchain.internal.InstallationLocation;

import javax.inject.Inject;
import java.nio.file.Path;

public class DefaultDcevmMetadataResolver implements IDcevmMetadataResolver {
    private final JvmMetadataDetector metadataDetector;
    private final ObjectFactory objectFactory;

    @Inject
    public DefaultDcevmMetadataResolver(ObjectFactory objectFactory, JvmMetadataDetector metadataDetector) {
        this.metadataDetector = metadataDetector;
        this.objectFactory = objectFactory;
    }

    @Override public DcevmMetadata resolveDcevmMetadata(Path javaHome) {
        InstallationLocation installationLocation = new InstallationLocation(javaHome.toFile(), "DefaultDcevmMetadataResolver");
        JvmInstallationMetadata installationMetadata = metadataDetector.getMetadata(installationLocation);
        JvmToolchainMetadata jvmToolchainMetadata = new JvmToolchainMetadata(installationMetadata, installationLocation);
        return objectFactory.newInstance(DefaultDcevmMetadata.class, jvmToolchainMetadata);
    }
}
