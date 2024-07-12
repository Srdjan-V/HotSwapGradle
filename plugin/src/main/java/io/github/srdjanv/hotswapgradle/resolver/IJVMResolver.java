package io.github.srdjanv.hotswapgradle.resolver;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.resolver.internal.DefaultJVMResolver;
import java.util.List;
import org.gradle.api.Project;
import org.gradle.internal.jvm.inspection.JavaInstallationRegistry;

/**
 * Used to query local jvm. The default impl is using gradle internal resolver
 * @see JavaInstallationRegistry
 * */
public interface IJVMResolver {
    static IJVMResolver of(Project project) {
        return project.getObjects().newInstance(DefaultJVMResolver.class);
    }

    List<DcevmMetadata> getAllDcevmToolchains(IDcevmMetadataResolver metadataResolver);
}
