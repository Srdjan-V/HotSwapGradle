/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.github.srdjanv.hotswapgradle;

import io.github.srdjanv.hotswapgradle.extentions.HotswapExtension;
import io.github.srdjanv.hotswapgradle.util.PropsUtil;
import javax.inject.Inject;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.build.event.BuildEventsListenerRegistry;
import org.jetbrains.annotations.NotNull;

public abstract class HotswapGradlePlugin implements Plugin<Project> {

    @Inject
    public abstract BuildEventsListenerRegistry getEventsListenerRegistry();

    @Override
    public void apply(@NotNull final Project project) {
        var serviceProvider = project.getGradle()
                .getSharedServices()
                .registerIfAbsent(HotswapGradleService.class.getName(), HotswapGradleService.class, spec -> {
                    var params = spec.getParameters();
                    params.getWorkingDirectory().set(PropsUtil.getWorkingDir(project));
                    params.getDebug().set(PropsUtil.isDebug(project));
                    params.getOfflineMode().set(PropsUtil.isOfflineMode(project));
                    params.getIsCachedRegistryPersistent().set(PropsUtil.isCachedRegistryPersistent(project));
                });

        getEventsListenerRegistry().onTaskCompletion(serviceProvider);
        project.getExtensions().create(HotswapExtension.NAME, HotswapExtension.class);
    }
}
