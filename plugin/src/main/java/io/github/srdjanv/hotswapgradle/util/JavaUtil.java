package io.github.srdjanv.hotswapgradle.util;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JavaToolchainSpec;

public class JavaUtil {

    public static Provider<JavaLauncher> getJavaLauncher(Project project, Action<? super JavaToolchainSpec> action) {
        return getToolchainService(project).launcherFor(action);
    }

    public static JavaToolchainService getToolchainService(Project project) {
       return project.getExtensions().getByType(JavaToolchainService.class);
    }

    public static JavaPluginExtension getJavaPluginExtension(Project project) {
        return project.getExtensions().getByType(JavaPluginExtension.class);
    }


    public static JavaVersion versionOf(Provider<JavaLanguageVersion> languageVersion) {
        return versionOf(languageVersion.get());
    }

    public static JavaVersion versionOf(DcevmSpec spec) {
        if (spec.getLanguageVersion().isPresent()) {
            return versionOf(spec.getLanguageVersion());
        }
        return versionOf(spec.getFallbackSpeck().get().getLanguageVersion());
    }

    public static JavaVersion versionOf(Property<JavaLanguageVersion> languageVersion) {
        return versionOf(languageVersion.get());
    }

    public static JavaVersion versionOf(JavaLanguageVersion languageVersion) {
        return JavaVersion.toVersion(languageVersion.asInt());
    }
}
