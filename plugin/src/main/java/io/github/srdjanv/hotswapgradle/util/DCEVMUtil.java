package io.github.srdjanv.hotswapgradle.util;

import com.github.dcevm.installer.ConfigurationInfo;
import com.github.dcevm.installer.Installation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class DCEVMUtil {

    public static boolean isDCEVMInstalledLikeAltJvm(Path jdkPath)  {
        try {
            return new Installation(ConfigurationInfo.current(), jdkPath).isDCEInstalledAltjvm();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean isDCEVMPresent(Path jdkPath) {
        Installation installation = null;
        try {
            installation = new Installation(ConfigurationInfo.current(), jdkPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return installation.isDCEInstalled() || installation.isDCEInstalledAltjvm();
    }

    public static String determineDCEVMVersion(Path jdkPath) {
        Installation installation;
        try {
            installation = new Installation(ConfigurationInfo.current(), jdkPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (installation.isDCEInstalled()) {
            return installation.getVersionDcevm();
        }

        if (installation.isDCEInstalledAltjvm()) {
            return installation.getVersionDcevmAltjvm();
        }

        return null;
    }
}
