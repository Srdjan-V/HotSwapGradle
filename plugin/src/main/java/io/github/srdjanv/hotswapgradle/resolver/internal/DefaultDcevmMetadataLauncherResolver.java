package io.github.srdjanv.hotswapgradle.resolver.internal;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataLauncherResolver;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.internal.DefaultToolchainJavaLauncher;
import org.gradle.jvm.toolchain.internal.JavaToolchain;

import javax.inject.Inject;

public class DefaultDcevmMetadataLauncherResolver implements IDcevmMetadataLauncherResolver {
    private final ProviderFactory providerFactory;

    @Inject
    public DefaultDcevmMetadataLauncherResolver(ProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
    }

    @Override public Provider<JavaLauncher> resolveLauncher(DcevmMetadata metadata) {
        return providerFactory.provider(() -> new DefaultToolchainJavaLauncher((JavaToolchain) metadata.getJavaInstallationMetadata().get()));
    }
}
