package io.github.srdjanv.hotswapgradle.resolver;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.resolver.internal.DefaultDcevmMetadataResolver;
import org.gradle.api.Project;

import java.nio.file.Path;

public interface IDcevmMetadataResolver {
    static IDcevmMetadataResolver of(Project project) {
        return project.getObjects().newInstance(DefaultDcevmMetadataResolver.class);
    }

    DcevmMetadata resolveDcevmMetadata(Path javaHome);
}
