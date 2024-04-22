package io.github.srdjanv.hotswapgradle;

import io.github.srdjanv.hotswapgradle.agent.HotswapAgentProvider;
import io.github.srdjanv.hotswapgradle.dcevmdetection.legacy.LegacyDcevmDetection;
import io.github.srdjanv.hotswapgradle.registry.ICashedJVMRegistry;
import io.github.srdjanv.hotswapgradle.registry.IKnownDcevmRegistry;
import io.github.srdjanv.hotswapgradle.registry.ILocalJVMRegistry;
import io.github.srdjanv.hotswapgradle.registry.internal.CashedJVMRegistry;
import io.github.srdjanv.hotswapgradle.registry.internal.KnownDcevmRegistry;
import io.github.srdjanv.hotswapgradle.registry.internal.LocalJVMRegistry;
import io.github.srdjanv.hotswapgradle.util.Constants;
import io.github.srdjanv.hotswapgradle.util.FileUtils;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.tooling.events.FinishEvent;
import org.gradle.tooling.events.OperationCompletionListener;
import org.gradle.tooling.events.task.TaskFinishEvent;

public abstract class HotswapGradleService
        implements BuildService<HotswapGradleService.HotSwapParameters>, OperationCompletionListener {
    public interface HotSwapParameters extends BuildServiceParameters {
        DirectoryProperty getWorkingDirectory();

        Property<Boolean> getDebug();

        Property<String> getAgentApiUrl();

        Property<Boolean> getOfflineMode();

        Property<Boolean> getIsCachedRegistryPersistent();
    }

    private final HotswapAgentProvider downloader;
    private final IKnownDcevmRegistry knownDCEVMRegistry;
    private final ICashedJVMRegistry cashedJVMRegistry;
    private final ILocalJVMRegistry localJVMRegistry;

    public HotswapGradleService() {
        downloader = new HotswapAgentProvider(
                Constants.AGENT_RELEASE_API_URL, getParameters().getWorkingDirectory());
        knownDCEVMRegistry = new KnownDcevmRegistry(this);
        cashedJVMRegistry = new CashedJVMRegistry(
                LegacyDcevmDetection::isPresent,
                FileUtils.jdkData(getParameters().getWorkingDirectory()).getAsFile());
        localJVMRegistry = new LocalJVMRegistry(cashedJVMRegistry);
    }

    public HotswapAgentProvider getAgentProvider() {
        return downloader;
    }

    public IKnownDcevmRegistry getKnownDCEVMRegistry() {
        return knownDCEVMRegistry;
    }

    public ICashedJVMRegistry getCashedJVMRegistry() {
        return cashedJVMRegistry;
    }

    public ILocalJVMRegistry getLocalJVMRegistry() {
        return localJVMRegistry;
    }

    @Override
    public void onFinish(FinishEvent event) {
        if (event instanceof TaskFinishEvent) cashedJVMRegistry.saveRegistry();
    }
}
