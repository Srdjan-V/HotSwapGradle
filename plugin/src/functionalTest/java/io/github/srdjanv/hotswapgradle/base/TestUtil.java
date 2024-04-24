package io.github.srdjanv.hotswapgradle.base;

import java.io.File;
import java.util.Collections;
import java.util.function.Consumer;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

public class TestUtil {

    public static GradleRunner provideTestRunner(
            File home,
            Consumer<GradleRunner> gradleConfig,
            Consumer<SettingsFile> settingsConfig,
            Consumer<ProjectFile> projectConfig) {
        GradleRunner runner = GradleRunner.create();
        runner.withProjectDir(home);
        gradleConfig.accept(runner);

        var thisSettingsFile = new SettingsFile(home);
        settingsConfig.accept(thisSettingsFile);
        thisSettingsFile.buildFile();

        var thisProjectFile = new ProjectFile(home);
        projectConfig.accept(thisProjectFile);
        thisProjectFile.buildFile();

        return runner;
    }

    public static boolean isAgentActive(BuildResult buildResult) {
        return isAgentActive(buildResult.getOutput());
    }

    public static boolean isAgentActive(String outLog) {
        return outLog.contains("org.hotswap.agent.HotswapAgent");
    }

    public static void defaultConfig(GradleRunner config) {
        config.forwardOutput();
        config.withPluginClasspath();
        config.withDebug(true);
        //config.withEnvironment(Collections.singletonMap(""))
    }
}
