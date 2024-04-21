package io.github.srdjanv.hotswapgradle.util;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;
import org.gradle.api.file.RegularFile;
import org.jetbrains.annotations.Nullable;

public class FileIOUtils {
    public static void saveStringToFile(String string, RegularFile file) {
        saveStringToFile(string, file.getAsFile());
    }

    public static void saveStringToFile(String string, File file) {
        var filePath = file.toPath();
        var folderPath = filePath.getParent();

        createFile(filePath);
        createDirs(folderPath);

        try (var out = Files.newBufferedWriter(
                filePath, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            out.write(string);
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
        var filePath = file.toPath();
        var folderPath = filePath.getParent();

        createFile(filePath);
        createDirs(folderPath);

        if (!Files.isRegularFile(filePath)) return null;
        try (var out = Files.newBufferedReader(filePath)) {
            return out.lines().collect(Collectors.joining());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void createDirs(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void createFile(Path file) {
        try {
            if (!Files.exists(file)) Files.createFile(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
