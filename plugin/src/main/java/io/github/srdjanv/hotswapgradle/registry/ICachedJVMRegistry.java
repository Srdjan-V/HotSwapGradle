package io.github.srdjanv.hotswapgradle.registry;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.locators.ICachedDcevmLocator;
import io.github.srdjanv.hotswapgradle.suppliers.CachedDcevmSupplier;
import org.gradle.api.JavaVersion;

public interface ICachedJVMRegistry extends ICachedDcevmLocator {
    void saveRegistry();

    void populateRegistry(JavaVersion javaVersion, CachedDcevmSupplier cachedDcevmSupplier);

    void addToRegistry(DcevmMetadata metadata);
}
