package io.github.srdjanv.hotswapgradle.resolver.internal;

import io.github.srdjanv.hotswapgradle.HotswapGradleService;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.dcvm.internal.DefaultDcevmSpec;
import io.github.srdjanv.hotswapgradle.resolver.IDcevmSpecResolver;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

public class DefaultDcevmSpecResolver implements IDcevmSpecResolver {

    private final ObjectFactory objectFactory;

    @Inject
    public DefaultDcevmSpecResolver(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public DcevmSpec resolveDcevmSpec(HotswapGradleService hotSwapGradleService, Action<? super DcevmSpec> action) {
        return configureToolchainSpec(hotSwapGradleService, action);
    }

    private DefaultDcevmSpec configureToolchainSpec(
            HotswapGradleService hotSwapGradleService, Action<? super DcevmSpec> config) {
        DefaultDcevmSpec toolchainSpec = objectFactory.newInstance(DefaultDcevmSpec.class, hotSwapGradleService);
        config.execute(toolchainSpec);
        return toolchainSpec;
    }
}
