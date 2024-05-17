package io.github.srdjanv.hotswapgradle.registry;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataResolver;
import java.nio.file.Path;

public interface IDcevmMetadataCacheRegistry {
    DcevmMetadata getResolveMetadata(Path javaHome, IDcevmMetadataResolver resolver);
}
