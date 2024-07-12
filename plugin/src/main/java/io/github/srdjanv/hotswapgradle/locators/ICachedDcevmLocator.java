package io.github.srdjanv.hotswapgradle.locators;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataLauncherResolver;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataResolver;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.jetbrains.annotations.Nullable;

/**
 * Used to locate a previously resolved dcevm vm
 * */
public interface ICachedDcevmLocator {
    @Nullable
    JavaLauncher locateVM(
            IDcevmMetadataResolver metadataResolver,
            IDcevmMetadataLauncherResolver metadataLauncherResolver,
            DcevmSpec dcevmSpec);
}
