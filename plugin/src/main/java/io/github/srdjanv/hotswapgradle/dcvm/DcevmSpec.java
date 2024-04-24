package io.github.srdjanv.hotswapgradle.dcvm;

import io.github.srdjanv.hotswapgradle.agent.AgentType;
import io.github.srdjanv.hotswapgradle.registry.IKnownDcevmRegistry;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.jvm.toolchain.JavaToolchainSpec;

public interface DcevmSpec extends JavaToolchainSpec {
    /**
     * If the query for a dcevm fails, this will supply the used jvm
     */
    Property<JavaToolchainSpec> getFallbackSpeck();

    /**
     * Metadata about the used jvm
     */
    Property<DcevmMetadata> getDcevmMetadata();

    /**
     * The HotSwap Agent type <p>
     * Use the string values of thees enums: {@link AgentType#DEFAULT }, {@link AgentType#CORE }
     */
    Property<String> getAgentType();

    /**
     * Sets if the HotSwap Agent version should be a snapshot release <p>
     * Default false
     */
    Property<Boolean> getUseSnapshot();

    /**
     * Sets the HotSwap Agent version based on the git release tag
     */
    Property<String> getGitTagName();

    /**
     * Holds the HotSwap Agent jar
     */
    RegularFileProperty getAgentJar();

    /**
     * Can set custom launch arguments. <p> Example:
     * <pre>
     * {@code
     *  getArguments().set(providerFactory.provider(() -> {
     *      var meta = getDcevmMetadata().getOrNull();
     *      if (meta != null && meta.getIsDcevmInstalledLikeAltJvm().get())
     *          return Arrays.asList("-XXaltjvm=dcevm",
     *              String.format("-javaagent:%s",
     *                  getAgentJar().get().getAsFile().getAbsolutePath()));
     *
     *      return Collections.singletonList(String.format("-javaagent:%s",
     *          getAgentJar().get().getAsFile().getAbsolutePath()));
     * }))
     * }
     * </pre>
     */
    ListProperty<String> getArguments();

    /**
     * If this spec should be resolved in the known dcevm registry <p>
     * For example, if you request a java 11 vm, this may return a Jetbrains jvm.
     * Since they are known to include DCEVM<p>
     * For all know jvm's see {@link IKnownDcevmRegistry#setDefaultRegistry()}
     */
    Property<Boolean> getQueryKnownDEVMs();

    /**
     * If this spec should be resolved in the known dcevm cached registry <p>
     * Once a Dcevm is found it will get cached, this cache is persistent
     */
    Property<Boolean> getQueryCachedDEVMs();

    /**
     * This will use Gradle's internal classes to find any local Dcevm installations
     */
    Property<Boolean> getQueryLocalDEVMs();
}
