package io.github.srdjanv.hotswapgradle.util;

import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;

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
}
