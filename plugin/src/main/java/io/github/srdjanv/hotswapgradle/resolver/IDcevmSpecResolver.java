package io.github.srdjanv.hotswapgradle.resolver;

import io.github.srdjanv.hotswapgradle.HotswapGradleService;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.resolver.internal.DefaultDcevmSpecResolver;
import org.gradle.api.Action;
import org.gradle.api.Project;

public interface IDcevmSpecResolver {
    static IDcevmSpecResolver of(Project project) {
        return project.getObjects().newInstance(DefaultDcevmSpecResolver.class);
    }

    DcevmSpec resolveDcevmSpec(HotswapGradleService hotSwapGradleService, Action<? super DcevmSpec> action);
}
