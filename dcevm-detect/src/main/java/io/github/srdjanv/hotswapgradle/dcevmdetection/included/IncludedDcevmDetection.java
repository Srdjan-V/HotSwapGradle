package io.github.srdjanv.hotswapgradle.dcevmdetection.included;

import io.github.srdjanv.hotswapgradle.dcevmdetection.legacy.ConfigurationInfo;

import java.nio.file.Path;

public class IncludedDcevmDetection {
    public static boolean isPresent(Path jdkPath) {
        try {
            //todo implement jvm probing
            var config = ConfigurationInfo.current();
            var consoleOut  = config.executeJava(jdkPath, "-XX:+AllowEnhancedClassRedefinition");
            return !consoleOut.contains("Unrecognized VM option 'AllowEnhancedClassRedefinition'");
        } catch (Exception ignore) {
        }
        return false;
    }
}
