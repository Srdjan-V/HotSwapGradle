package io.github.srdjanv.hotswapgradle.dcevmdetection.legacy;

import java.nio.file.Path;

public class LegacyDcevmDetection {
    public static boolean isPresent(Path jdkPath) {
        try {
            var config = ConfigurationInfo.current();
            return config.isDCEInstalled(jdkPath, false) || config.isDCEInstalled(jdkPath, true);
        } catch (Exception ignore) {
        }
        return false;
    }

    public static boolean isInstalledLikeAltJvm(Path jdkPath) {
        try {
            return ConfigurationInfo.current().isDCEInstalled(jdkPath, true);
        } catch (Exception ignore) {
        }
        return false;
    }

    public static String determineDCEVMVersion(Path jdkPath) {
        String dcevmVersion = "NONE";
        try {
            boolean present;
            var config = ConfigurationInfo.current();
            present = config.isDCEInstalled(jdkPath, false);
            if (present) dcevmVersion = config.getDCEVersion(jdkPath, false);

            if (!present) {
                present = config.isDCEInstalled(jdkPath, true);
                if (present) dcevmVersion = config.getDCEVersion(jdkPath, true);
            }
        } catch (Exception ignore) {
        }
        return dcevmVersion;
    }
}
