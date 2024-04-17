package io.github.srdjanv.hotswapgradle.resolver;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.resolver.internal.DefaultDcevmMetadataLauncherResolver;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.jetbrains.annotations.Nullable;

public interface IDcevmMetadataLauncherResolver {
    static IDcevmMetadataLauncherResolver of(Project project) {
        return project.getObjects().newInstance(DefaultDcevmMetadataLauncherResolver.class);
    }

    @Nullable Provider<JavaLauncher> resolveLauncher(DcevmMetadata metadata);
}
