package io.github.srdjanv.hotswapgradle.suppliers;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;

import java.util.List;

@FunctionalInterface
public interface KnownDcevmSupplier {
    List<Action<? super DcevmSpec>> getKnownDCEVMs();
}
