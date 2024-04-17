package io.github.srdjanv.hotswapgradle.resolver.internal;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.resolver.ILauncherResolver;
import io.github.srdjanv.hotswapgradle.util.JavaUtil;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JavaToolchainSpec;

import javax.inject.Inject;

public class DefaultLauncherResolver implements ILauncherResolver {
    private final JavaToolchainService javaToolchainService;

    @Inject
    public DefaultLauncherResolver(Project project) {
        javaToolchainService = JavaUtil.getToolchainService(project);
    }

    @Override public Provider<JavaLauncher> resolveLauncher(DcevmSpec dcevmSpec) {
        return javaToolchainService.launcherFor(dcevmSpec);
    }

    @Override public Provider<JavaLauncher> resolveLauncher(JavaToolchainSpec spec) {
        return javaToolchainService.launcherFor(spec);
    }
}
