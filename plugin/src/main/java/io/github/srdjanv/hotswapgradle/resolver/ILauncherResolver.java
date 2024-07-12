package io.github.srdjanv.hotswapgradle.resolver;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.resolver.internal.DefaultLauncherResolver;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JavaToolchainSpec;

/**
 * Wrapper for gradle JavaToolchainService
 * @see JavaToolchainService
 * */
public interface ILauncherResolver {
    static ILauncherResolver of(Project project) {
        return project.getObjects().newInstance(DefaultLauncherResolver.class);
    }

    Provider<JavaLauncher> resolveLauncher(DcevmSpec dcevmSpec);

    Provider<JavaLauncher> resolveLauncher(JavaToolchainSpec spec);
}
