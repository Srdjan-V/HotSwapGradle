package io.github.srdjanv.hotswapgradle.resolver;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.resolver.internal.DefaultJVMResolver;
import org.gradle.api.Project;

import java.util.List;

public interface IJVMResolver {
    static IJVMResolver of(Project project) {
        return project.getObjects().newInstance(DefaultJVMResolver.class);
    }

    List<DcevmMetadata> getAllDcevmToolchains(IDcevmMetadataResolver metadataResolver);
}
