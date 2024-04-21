package io.github.srdjanv.hotswapgradle.dcvm.internal;

import io.github.srdjanv.hotswapgradle.dcevmdetection.legacy.LegacyDcevmDetection;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.internal.file.FileFactory;
import org.gradle.api.provider.Provider;
import org.gradle.internal.jvm.inspection.JvmToolchainMetadata;
import org.gradle.jvm.toolchain.*;
import org.gradle.jvm.toolchain.internal.DefaultToolchainSpec;
import org.gradle.jvm.toolchain.internal.JavaToolchain;
import org.gradle.jvm.toolchain.internal.JavaToolchainInput;

public class DefaultDcevmMetadata implements DcevmMetadata {
    private final JvmToolchainMetadata jvmToolchainMetadata;
    private final Provider<JavaInstallationMetadata> javaInstallationMetadata;
    private final Provider<Boolean> isDcevmPresent;
    private final Provider<Boolean> isDcevmInstalledLikeAltJvm;
    private final Provider<String> dcevmVersion;

    @Inject
    public DefaultDcevmMetadata(JvmToolchainMetadata jvmToolchainMetadata) {
        this.jvmToolchainMetadata = jvmToolchainMetadata;

        JavaToolchainSpec spec = new DefaultToolchainSpec(getProject().getObjects());
        spec.getLanguageVersion()
                .set(JavaLanguageVersion.of(
                        jvmToolchainMetadata.metadata.getLanguageVersion().getMajorVersion()));
        spec.getVendor()
                .set(JvmVendorSpec.matching(
                        jvmToolchainMetadata.metadata.getVendor().getRawVendor()));

        var providerFactory = getProject().getProviders();
        javaInstallationMetadata = providerFactory.provider(() -> new JavaToolchain(
                jvmToolchainMetadata.metadata, getFileFactory(), new JavaToolchainInput(spec), false));
        isDcevmPresent = providerFactory.provider(() -> LegacyDcevmDetection.isPresent(
                javaInstallationMetadata.get().getInstallationPath().getAsFile().toPath()));
        isDcevmInstalledLikeAltJvm = providerFactory.provider(() -> LegacyDcevmDetection.isInstalledLikeAltJvm(
                javaInstallationMetadata.get().getInstallationPath().getAsFile().toPath()));
        dcevmVersion = providerFactory.provider(() -> LegacyDcevmDetection.determineDCEVMVersion(
                javaInstallationMetadata.get().getInstallationPath().getAsFile().toPath()));
    }

    public JvmToolchainMetadata getJvmToolchainMetadata() {
        return jvmToolchainMetadata;
    }

    @Override
    public Provider<JavaInstallationMetadata> getJavaInstallationMetadata() {
        return javaInstallationMetadata;
    }

    @Override
    public Provider<Boolean> getIsDcevmPresent() {
        return isDcevmPresent;
    }

    @Override
    public Provider<Boolean> getIsDcevmInstalledLikeAltJvm() {
        return isDcevmInstalledLikeAltJvm;
    }

    @Override
    public Provider<String> getDcevmVersion() {
        return dcevmVersion;
    }

    @Inject
    public FileFactory getFileFactory() {
        throw new UnsupportedOperationException();
    }

    @Inject
    public Project getProject() {
        throw new UnsupportedOperationException();
    }
}
