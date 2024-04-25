package io.github.srdjanv.hotswapgradle.registry;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.locators.ICachedDcevmLocator;
import io.github.srdjanv.hotswapgradle.registry.internal.CashedJVMRegistry;
import io.github.srdjanv.hotswapgradle.suppliers.CachedDcevmSupplier;
import io.github.srdjanv.hotswapgradle.validator.DcevmValidator;
import org.gradle.api.JavaVersion;
import org.jetbrains.annotations.NotNull;

public interface ICashedJVMRegistry extends ICachedDcevmLocator {
    void saveRegistry();

    void populateRegistry(JavaVersion javaVersion,CachedDcevmSupplier cachedDcevmSupplier);

    void addToRegistry(DcevmMetadata metadata);
}
