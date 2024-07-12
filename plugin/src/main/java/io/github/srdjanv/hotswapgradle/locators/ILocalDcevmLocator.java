package io.github.srdjanv.hotswapgradle.locators;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.resolver.*;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.jetbrains.annotations.Nullable;

/**
 * Used to query vms on the local machine
 * */
public interface ILocalDcevmLocator {
    @Nullable
    JavaLauncher locateVM(
            IJVMResolver jvmResolver,
            IDcevmMetadataResolver metadataResolver,
            IDcevmMetadataLauncherResolver launcherResolver,
            DcevmSpec dcevmSpec);

    JavaLauncher locateVanillaVM(ILauncherResolver launcherResolver, DcevmSpec dcevmSpec);
}
