package io.github.srdjanv.hotswapgradle.base;

import java.io.File;
import java.util.function.Supplier;

public class SettingsFile extends BaseConfig {
    public SettingsFile(File projectDir) {
        super(projectDir, "settings.gradle");
    }

    public enum Plugins implements Supplier<String> {
        FOOJAY(
                """
                    // Automatic toolchain provisioning
                    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
                """);

        private final String action;

        Plugins(String action) {
            this.action = action;
        }

        @Override
        public String get() {
            return action;
        }
    }
}
