/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.github.srdjanv.hotswapgradle;

import io.github.srdjanv.hotswapgradle.extentions.HotswapExtension;
import javax.inject.Inject;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.build.event.BuildEventsListenerRegistry;
import org.jetbrains.annotations.NotNull;

public abstract class HotSwapGradlePlugin implements Plugin<Project> {

    @Inject
    public abstract BuildEventsListenerRegistry getEventsListenerRegistry();

    @Override
    public void apply(@NotNull Project project) {
        var serviceProvider = project.getGradle()
                .getSharedServices()
                .registerIfAbsent(
                        HotswapGradleService.class.getName(), HotswapGradleService.class, spec -> spec.getParameters()
                                .getWorkingDirectory()
                                .set(project.getGradle().getGradleUserHomeDir()));

        getEventsListenerRegistry().onTaskCompletion(serviceProvider);
        project.getExtensions().create(HotswapExtension.NAME, HotswapExtension.class);
    }
}
