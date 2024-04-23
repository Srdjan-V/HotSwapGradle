package io.github.srdjanv.hotswapgradle.base;

import java.io.File;
import java.util.function.Supplier;

public class ProjectFile extends BaseConfig {
    public ProjectFile(File projectDir) {
        super(projectDir, "build.gradle");
    }

    public void setMainCodeEmpty() {
        setMainCode("");
    }

    public void setMainCodeJDKLOG() {
        setMainCode("System.out.println(System.getProperty(\"java.home\"));");
    }

    public void setMainCode(String code) {
        setMainClass(String.format("""
                public class Main {
                    public static void main(String[] args) {
                        %s
                    }
                }
                """, code));
    }

    public void setMainClass(String clazz) {
        write(new File(projectDir, "src/main/java/Main.java"), clazz);
    }

    public enum Plugins implements Supplier<String> {
        HOTSWAP("id('io.github.srdjan-v.hotswap-gradle')"),
        APPLICATION("id('application')");

        private final String action;

        Plugins(String action) {
            this.action = action;
        }

        @Override
        public String get() {
            return action;
        }
    }

    public enum Options implements Supplier<String> {
        APPLICATION_MAIN("""
                application {
                    mainClass = 'Main'
                }
                """),

        REQUEST_HOTSWAP_JDK_8(
                """
                        hotswapProvider {
                            configureTask(tasks.named('run')) {
                                languageVersion = JavaLanguageVersion.of(8)
                            }
                        }
                        """),
        REQUEST_HOTSWAP_JDK_11(
                """
                        hotswapProvider {
                            configureTask(tasks.named('run')) {
                                languageVersion = JavaLanguageVersion.of(11)
                            }
                        }
                        """),
        REQUEST_HOTSWAP_JDK_17(
                """
                        hotswapProvider {
                            configureTask(tasks.named('run')) {
                                languageVersion = JavaLanguageVersion.of(17)
                            }
                        }
                        """),
        REQUEST_HOTSWAP_JDK_21(
                """
                        hotswapProvider {
                            configureTask(tasks.named('run')) {
                                languageVersion = JavaLanguageVersion.of(21)
                            }
                        }
                        """);

        private final String action;

        Options(String action) {
            this.action = action;
        }

        @Override
        public String get() {
            return action;
        }
    }
}
