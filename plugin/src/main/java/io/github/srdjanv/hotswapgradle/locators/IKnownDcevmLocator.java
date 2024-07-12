package io.github.srdjanv.hotswapgradle.locators;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataResolver;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmSpecResolver;
import io.github.srdjanv.hotswapgradle.resolver.ILauncherResolver;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.jetbrains.annotations.Nullable;

/**
 * Used to locate a known dcevm vm. Example JBR
 * @see io.github.srdjanv.hotswapgradle.registry.IKnownDcevmRegistry
 * */
public interface IKnownDcevmLocator {
    @Nullable
    JavaLauncher locateVM(
            IDcevmMetadataResolver metadataResolver,
            ILauncherResolver resolver,
            IDcevmSpecResolver specResolver,
            DcevmSpec dcevmSpec);
}
