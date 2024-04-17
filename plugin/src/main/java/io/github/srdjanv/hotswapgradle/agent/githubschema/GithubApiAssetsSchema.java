package io.github.srdjanv.hotswapgradle.agent.githubschema;

import com.github.bsideup.jabel.Desugar;
import io.github.srdjanv.hotswapgradle.agent.AgentType;

@Desugar
public record GithubApiAssetsSchema(
        String name,
        String content_type,
        String browser_download_url) {
    public boolean isValidJar(AgentType agentType) {
        return switch (agentType) {
            case DEFAULT -> isAgentJar();
            case CORE -> isCoreAgentJar();
        };
    }

    public boolean isAgentJar() {
        return isJavaArchive() && !name.contains("sources") && !name.contains("core");
    }

    public boolean isCoreAgentJar() {
        return isJavaArchive() && name.contains("core");
    }

    public boolean isJavaArchive() {
        if (content_type.startsWith("application"))
            return content_type.endsWith("x-java-archive")
                    || content_type.endsWith("java-archive")
                    || content_type.endsWith("octet-stream");
        return false;

    }
}
