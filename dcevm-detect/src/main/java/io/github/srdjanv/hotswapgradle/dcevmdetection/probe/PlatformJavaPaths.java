package io.github.srdjanv.hotswapgradle.dcevmdetection.probe;

import java.nio.file.Files;
import java.nio.file.Path;

public enum PlatformJavaPaths {
    MAC_OS("bin/java"),
    LINUX("bin/java"),
    WINDOWS("bin/java.exe");

    public static Path resolveExecutable(Path javaHome) {
        if (Files.isRegularFile(javaHome)) throw new IllegalArgumentException("Java home is a file");
        if (Files.isDirectory(javaHome.resolve("jre"))) javaHome = javaHome.resolve("jre");
        return javaHome.resolve(get().path);
    }

    public static PlatformJavaPaths get() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("windows")) {
            return WINDOWS;
        } else if (os.contains("mac") || os.contains("darwin")) {
            return MAC_OS;
        } else if (os.contains("unix") || os.contains("linux")) {
            return LINUX;
        }
        throw new IllegalStateException("OS is unsupported: " + os);
    }

    private final String path;

    PlatformJavaPaths(String s) {
        path = s;
    }
}
