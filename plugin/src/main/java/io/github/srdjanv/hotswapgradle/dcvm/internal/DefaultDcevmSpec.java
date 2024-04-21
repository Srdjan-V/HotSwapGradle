package io.github.srdjanv.hotswapgradle.dcvm.internal;

import io.github.srdjanv.hotswapgradle.HotswapGradleService;
import io.github.srdjanv.hotswapgradle.agent.AgentType;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmMetadata;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.util.JavaUtil;
import java.util.Arrays;
import java.util.Collections;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.jvm.toolchain.JavaToolchainSpec;
import org.gradle.jvm.toolchain.internal.DefaultToolchainSpec;

public class DefaultDcevmSpec extends DefaultToolchainSpec implements DcevmSpec {
    private final Property<JavaToolchainSpec> fallbackSpeck;
    private final Property<DcevmMetadata> dcevmMetadataProvider;
    private final Property<String> agentType;
    private final Property<Boolean> useSnapshot;
    private final Property<String> gitTagName;
    private final RegularFileProperty agentJar;
    private final ListProperty<String> arguments;
    private final Property<Boolean> queryKnownDEVMs;
    private final Property<Boolean> queryCachedDEVMs;
    private final Property<Boolean> queryLocalDEVMs;

    @Inject
    public DefaultDcevmSpec(
            ObjectFactory objectFactory,
            ProviderFactory providerFactory,
            ProjectLayout projectLayout,
            Project project,
            HotswapGradleService provider) {
        super(objectFactory);

        var java = JavaUtil.getJavaPluginExtension(project);
        fallbackSpeck = objectFactory.property(JavaToolchainSpec.class).convention(java.getToolchain());
        dcevmMetadataProvider = objectFactory.property(DcevmMetadata.class);

        agentType = objectFactory.property(String.class).convention(AgentType.DEFAULT.toString());
        useSnapshot = objectFactory.property(Boolean.class).convention(false);
        gitTagName = objectFactory.property(String.class);

        agentJar = objectFactory
                .fileProperty()
                .convention(projectLayout.file(providerFactory.provider(
                        () -> provider.getAgentProvider().requestAgent(this).toFile())));

        arguments = objectFactory.listProperty(String.class).convention(providerFactory.provider(() -> {
            var meta = getDcevmMetadata().getOrNull();
            if (meta != null && meta.getIsDcevmInstalledLikeAltJvm().get())
                return Arrays.asList(
                        "-XXaltjvm=dcevm",
                        String.format(
                                "-javaagent:%s", agentJar.get().getAsFile().getAbsolutePath()));

            return Collections.singletonList(
                    String.format("-javaagent:%s", agentJar.get().getAsFile().getAbsolutePath()));
        }));

        queryKnownDEVMs = objectFactory.property(Boolean.class).convention(true);
        queryCachedDEVMs = objectFactory.property(Boolean.class).convention(true);
        queryLocalDEVMs = objectFactory.property(Boolean.class).convention(true);
    }

    @Override
    public Property<JavaToolchainSpec> getFallbackSpeck() {
        return fallbackSpeck;
    }

    @Override
    public Property<DcevmMetadata> getDcevmMetadata() {
        return dcevmMetadataProvider;
    }

    @Override
    public Property<String> getAgentType() {
        return agentType;
    }

    @Override
    public Property<Boolean> getUseSnapshot() {
        return useSnapshot;
    }

    @Override
    public Property<String> getGitTagName() {
        return gitTagName;
    }

    @Override
    public RegularFileProperty getAgentJar() {
        return agentJar;
    }

    @Override
    public ListProperty<String> getArguments() {
        return arguments;
    }

    @Override
    public Property<Boolean> getQueryKnownDEVMs() {
        return queryKnownDEVMs;
    }

    @Override
    public Property<Boolean> getQueryCachedDEVMs() {
        return queryCachedDEVMs;
    }

    @Override
    public Property<Boolean> getQueryLocalDEVMs() {
        return queryLocalDEVMs;
    }
}
