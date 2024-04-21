package io.github.srdjanv.hotswapgradle.extentions;

import groovy.lang.GroovyObjectSupport;
import io.github.srdjanv.hotswapgradle.HotswapGradleService;
import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.resolver.*;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildServiceRegistration;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.toolchain.*;

public abstract class HotswapExtension extends GroovyObjectSupport {
    public static final String NAME = "hotswapProvider";

    private final Project project;
    private final Provider<HotswapGradleService> hotswapGradleService;

    @Inject
    public HotswapExtension(Project project) {
        this.project = project;
        project.getPlugins().apply(JavaBasePlugin.class);

        BuildServiceRegistration<?, ?> serviceRegistration = project.getGradle()
                .getSharedServices()
                .getRegistrations()
                .getByName(HotswapGradleService.class.getName());
        //noinspection unchecked
        hotswapGradleService = (Provider<HotswapGradleService>) serviceRegistration.getService();
    }

    public void configureTask(TaskProvider<? extends JavaExec> taskProvider) {
        taskProvider.configure(javaExec -> configureTask(javaExec, project, spec -> {}));
    }

    public void configureTask(TaskProvider<? extends JavaExec> taskProvider, Action<? super DcevmSpec> spec) {
        taskProvider.configure(javaExec -> configureTask(javaExec, project, spec));
    }

    public void configureTask(JavaExec task) {
        configureTask(task, project, spec -> {});
    }

    public void configureTask(JavaExec task, Action<? super DcevmSpec> spec) {
        configureTask(task, project, spec);
    }

    public void configureTask(JavaExec task, Project project, Action<? super DcevmSpec> spec) {
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
            var hotswapService = hotswapGradleService.get();
            var dcevmSpec = specResolver.resolveDcevmSpec(hotswapService, spec);

            JavaLauncher javaLauncher =
                    hotswapService.getKnownDCEVMRegistry().locateVM(launcherResolver, specResolver, dcevmSpec);
            if (javaLauncher == null)
                javaLauncher = hotswapService
                        .getCashedJVMRegistry()
                        .locateVM(metadataResolver, metadataLauncherResolver, dcevmSpec);
            if (javaLauncher == null)
                javaLauncher = hotswapService
                        .getLocalJVMRegistry()
                        .locateVM(jvmResolver, metadataResolver, metadataLauncherResolver, dcevmSpec);

            if (javaLauncher == null) {
                javaLauncher = hotswapService.getLocalJVMRegistry().locateVanillaVM(launcherResolver, dcevmSpec);
            } else task.jvmArgs(dcevmSpec.getArguments().get());
            return javaLauncher;
        }));
    }

    public Provider<HotswapGradleService> getHotswapGradleService() {
        return hotswapGradleService;
    }
}
