package io.github.srdjanv.hotswapgradle.agent;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record DownloadConfig(AgentType agentType, boolean snapshot, String tagName) {
    public DownloadConfig(AgentType agentType) {
        this(agentType, false, null);
    }

    public DownloadConfig(AgentType agentType, boolean snapshot) {
        this(agentType, snapshot, null);
    }
}
