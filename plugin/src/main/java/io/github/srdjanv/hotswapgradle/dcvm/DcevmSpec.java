package io.github.srdjanv.hotswapgradle.dcvm;

import io.github.srdjanv.hotswapgradle.agent.AgentType;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaToolchainSpec;

public interface DcevmSpec extends JavaToolchainSpec {
    Property<JavaToolchainSpec> getFallbackSpeck();
    Property<DcevmMetadata> getDcevmMetadata();
    /**
     * @see AgentType
     * */
    Property<String> getAgentType();
    Property<Boolean> getUseSnapshot();
    Property<String> getGitTagName();
    RegularFileProperty getAgentJar();
    ListProperty<String> getArguments();;
    Property<Boolean> getQueryKnownDEVMs();
    Property<Boolean> getQueryCachedDEVMs();
    Property<Boolean> getQueryLocalDEVMs();
}
