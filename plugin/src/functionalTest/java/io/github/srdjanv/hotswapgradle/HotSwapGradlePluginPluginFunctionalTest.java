/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.github.srdjanv.hotswapgradle;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class HotSwapGradlePluginPluginFunctionalTest {
    @TempDir
    File projectDir;

    private File getBuildFile() {
        return new File(projectDir, "build.gradle");
    }

    private File getSettingsFile() {
        return new File(projectDir, "settings.gradle");
    }

    @Test void canRunBuild() throws IOException {
        writeString(getSettingsFile(), "");
        writeString(getBuildFile(),
            "plugins {" +
            "  id('io.github.srdjan-v.hotswap-gradle')" +
            "}");

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withProjectDir(projectDir);
        runner.build();
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
