package io.github.srdjanv.hotswapgradle.util;

import java.io.File;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.gradle.api.Project;

public class PropsUtil {
    public static Boolean isDebug(Project project) {
        return getBoolOrDefault(project, "io.github.srdjan-v.hotswap-gradle.debug", false);
    }

    public static Boolean isCachedRegistryPersistent(Project project) {
        return getBoolOrDefault(project, "io.github.srdjan-v.hotswap-gradle.cached-registry-persistence", true);
    }

    public static String getAgentGitUrl(Project project) {
        return getStringOrDefault(
                project, "io.github.srdjan-v.hotswap-gradle.agent-api-url", Constants.AGENT_RELEASE_API_URL);
    }

    public static Boolean isOfflineMode(Project project) {
        return getBoolOrDefault(project, "io.github.srdjan-v.hotswap-gradle.offline-mode", () -> project.getGradle()
                .getStartParameter()
                .isOffline());
    }

    public static File getWorkingDir(Project project) {
        return getDirOrDefault(project, "io.github.srdjan-v.hotswap-gradle.work-directory", () -> project.getGradle()
                .getGradleUserHomeDir());
    }

    public static File getDirOrDefault(Project project, String name, Supplier<File> defaultValue) {
        return getDirOrDefault(project, name, defaultValue.get());
    }

    public static File getDirOrDefault(Project project, String name, File defaultValue) {
        var value = project.findProperty(name);
        return value instanceof String ? project.file(value) : defaultValue;
    }

    public static Boolean getBoolOrDefault(Project project, String name, BooleanSupplier defaultValue) {
        return getBoolOrDefault(project, name, defaultValue.getAsBoolean());
    }

    public static Boolean getBoolOrDefault(Project project, String name, boolean defaultValue) {
        var value = project.findProperty(name);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }

    public static String getStringOrDefault(Project project, String name, Supplier<String> defaultValue) {
        return getStringOrDefault(project, name, defaultValue.get());
    }

    public static String getStringOrDefault(Project project, String name, String defaultValue) {
        var value = project.findProperty(name);
        return value instanceof String ? (String) value : defaultValue;
    }
}
