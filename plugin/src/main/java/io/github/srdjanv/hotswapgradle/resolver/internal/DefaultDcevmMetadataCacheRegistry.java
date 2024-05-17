package io.github.srdjanv.hotswapgradle.resolver.internal;

import io.github.srdjanv.hotswapgradle.HotswapGradleService;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.registry.IDcevmMetadataCacheRegistry;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmMetadataResolver;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultDcevmMetadataCacheRegistry implements IDcevmMetadataCacheRegistry {
    private final Map<Path, DcevmMetadata> dcevmMetadataCache = new ConcurrentHashMap<>();
    private final HotswapGradleService service;

    public DefaultDcevmMetadataCacheRegistry(HotswapGradleService service) {
        this.service = service;
    }

    @Override
    public DcevmMetadata getResolveMetadata(Path javaHome, IDcevmMetadataResolver resolver) {
        return dcevmMetadataCache.computeIfAbsent(javaHome, resolver::resolveDcevmMetadata);
    }
}
