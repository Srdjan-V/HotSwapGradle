package io.github.srdjanv.hotswapgradle.dcvm;

import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaInstallationMetadata;

public interface DcevmMetadata {
    Provider<JavaInstallationMetadata> getJavaInstallationMetadata();
    Provider<Boolean> getIsDcevmPresent();
    Provider<Boolean> getIsDcevmInstalledLikeAltJvm();
    Provider<String> getDcevmVersion();
}
