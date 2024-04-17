package io.github.srdjanv.hotswapgradle.extentions;

import groovy.lang.GroovyObjectSupport;
import io.github.srdjanv.hotswapgradle.HotSwapGradleService;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.resolver.*;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildServiceRegistration;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.toolchain.*;

import javax.inject.Inject;

public abstract class HotSwapExtension extends GroovyObjectSupport {
    private final Project project;
    private final Provider<HotSwapGradleService> hotSwapGradleService;

    @Inject
    public HotSwapExtension(Project project) {
        this.project = project;
        project.getPlugins().apply(JavaPlugin.class);

        BuildServiceRegistration<?, ?> serviceRegistration = project
                .getGradle()
                .getSharedServices()
                .getRegistrations()
                .getByName(HotSwapGradleService.class.getName());
        //noinspection unchecked
        hotSwapGradleService = (Provider<HotSwapGradleService>) serviceRegistration.getService();
    }

    public void configureTask(TaskProvider<? extends JavaExec> taskProvider) {
        taskProvider.configure(javaExec ->
                configureTask(
                        javaExec,
                        project,
                        spec -> {
                        }));
    }

    public void configureTask(
            TaskProvider<? extends JavaExec> taskProvider,
            Action<? super DcevmSpec> spec) {
        taskProvider.configure(javaExec ->
                configureTask(
                        javaExec,
                        project,
                        spec));
    }

    public void configureTask(
            JavaExec task,
            Project project,
            Action<? super DcevmSpec> spec) {
        configureTask(
                task,
                IDcevmSpecResolver.of(project),
                ILauncherResolver.of(project),
                IDcevmMetadataLauncherResolver.of(project),
                IDcevmMetadataResolver.of(project),
                IJVMResolver.of(project),
                spec);
    }

    public void configureTask(
            JavaExec task,
            IDcevmSpecResolver specResolver,
            ILauncherResolver launcherResolver,
            IDcevmMetadataLauncherResolver metadataLauncherResolver,
            IDcevmMetadataResolver metadataResolver,
            IJVMResolver jvmResolver,
            Action<? super DcevmSpec> spec) {
        task.getJavaLauncher().set(project.provider(() -> {
            var hotswapService = hotSwapGradleService.get();
            var dcevmSpec = specResolver.resolveDcevmSpec(hotswapService, spec);

            JavaLauncher javaLauncher = hotswapService.getKnownDCEVMRegistry().locateVM(launcherResolver, specResolver, dcevmSpec);
            if (javaLauncher == null)
                javaLauncher = hotswapService.getCashedJVMRegistry().locateVM(metadataResolver, metadataLauncherResolver, dcevmSpec);
            if (javaLauncher == null)
                javaLauncher = hotswapService.getLocalJVMRegistry().locateVM(jvmResolver, metadataResolver, metadataLauncherResolver, dcevmSpec);

            if (javaLauncher == null) {
                javaLauncher = hotswapService.getLocalJVMRegistry().locateVanillaVM(launcherResolver, dcevmSpec);
            } else task.jvmArgs(dcevmSpec.getArguments().get());
            return javaLauncher;
        }));
    }

    public Provider<HotSwapGradleService> getHotSwapGradleService() {
        return hotSwapGradleService;
    }
}
