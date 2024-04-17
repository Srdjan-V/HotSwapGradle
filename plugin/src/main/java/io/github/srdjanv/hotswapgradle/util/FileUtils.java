package io.github.srdjanv.hotswapgradle.util;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
    public static RegularFile jdkData(DirectoryProperty workingDir) {
        return dataDir(workingDir).file("JDK-Data.json");
    }

    public static RegularFile agentVersion(DirectoryProperty workingDir) {
        return dataDir(workingDir).file("agent-versions.json");
    }

    public static Directory agentDir(DirectoryProperty workingDir) {
        return dataDir(workingDir).dir("hotswap-agent");
    }

    public static Directory dataDir(DirectoryProperty workingDir) {
        return workingDir.get().dir("caches/hotswapgradle");
    }

    public static void saveTestToFile(String testName, RegularFile file) {
        saveTestToFile(testName, file.getAsFile());
    }

    public static void saveTestToFile(String testName, File file) {
        var path = file.toPath();
        var parent = path.getParent();
        crateDirs(parent);
        crateFile(parent);

        try (var out = Files.newBufferedWriter(path,
                StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            out.write(testName);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Nullable
    public static String loadTextFromFile(RegularFile file) {
        return loadTextFromFile(file.getAsFile());
    }

    @Nullable
    public static String loadTextFromFile(File file) {
        var path = file.toPath();
        var parent = path.getParent();

        crateDirs(parent);
        crateFile(parent);

        if (!Files.isRegularFile(path)) return "";
        try (var out = Files.newBufferedReader(path)) {
            return out.lines().collect(Collectors.joining());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void crateDirs(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void crateFile(Path file) {
        try {
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
