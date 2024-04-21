package io.github.srdjanv.hotswapgradle.locators;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.resolver.ILauncherResolver;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmSpecResolver;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.jetbrains.annotations.Nullable;

public interface IDcevmLocator {
    @Nullable JavaLauncher locateVM(ILauncherResolver resolver, IDcevmSpecResolver specResolver, DcevmSpec dcevmSpec);
}