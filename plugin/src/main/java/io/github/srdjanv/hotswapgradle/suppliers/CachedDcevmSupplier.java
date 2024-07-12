package io.github.srdjanv.hotswapgradle.suppliers;

import java.nio.file.Path;
import java.util.List;

@FunctionalInterface
public interface CachedDcevmSupplier {
    List<Path> getLocalJvms();
}
