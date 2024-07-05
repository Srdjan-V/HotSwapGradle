package io.github.srdjanv.hotswapgradle.registry;

import io.github.srdjanv.hotswapgradle.dcvm.DcevmSpec;
import io.github.srdjanv.hotswapgradle.locators.IDcevmLocator;
import io.github.srdjanv.hotswapgradle.suppliers.KnownDcevmSupplier;
import java.util.*;
import java.util.function.Consumer;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JvmVendorSpec;

public interface IKnownDcevmRegistry extends IDcevmLocator {

    default void setDefaultRegistry() {
        configureRegistry(Map::clear);
        // linux only
        populateRegistry(
                JavaVersion.VERSION_1_8,
                () -> Collections.singletonList(dcevmSpec -> {
                    dcevmSpec.getLanguageVersion().set(JavaLanguageVersion.of(8));
                    dcevmSpec.getVendor().set(JvmVendorSpec.matching("trava"));
                    dcevmSpec
                            .getArguments()
                            .set(Collections.singletonList(String.format(
                                    "-javaagent:%s",
                                    dcevmSpec.getAgentJar().get().getAsFile().getAbsolutePath())));
                }));

        populateRegistry(JavaVersion.VERSION_11, () -> {
            List<Action<? super DcevmSpec>> jdk11 = new ArrayList<>();
            jdk11.add(dcevmSpec -> {
                dcevmSpec.getLanguageVersion().set(JavaLanguageVersion.of(11));
                dcevmSpec.getVendor().set(JvmVendorSpec.JETBRAINS);
                dcevmSpec
                        .getArguments()
                        .set(Arrays.asList(
                                "-XX:+AllowEnhancedClassRedefinition",
                                "-XX:HotswapAgent=external",
                                String.format(
                                        "-javaagent:%s",
                                        dcevmSpec
                                                .getAgentJar()
                                                .get()
                                                .getAsFile()
                                                .getAbsolutePath())));
            });

            jdk11.add(dcevmSpec -> {
                dcevmSpec.getLanguageVersion().set(JavaLanguageVersion.of(11));
                dcevmSpec.getVendor().set(JvmVendorSpec.matching("trava"));
            });

            return jdk11;
        });

        populateRegistry(
                JavaVersion.VERSION_17,
                () -> Collections.singletonList(dcevmSpec -> {
                    dcevmSpec.getLanguageVersion().set(JavaLanguageVersion.of(17));
                    dcevmSpec.getVendor().set(JvmVendorSpec.JETBRAINS);

                    dcevmSpec
                            .getArguments()
                            .set(Arrays.asList(
                                    "-XX:+AllowEnhancedClassRedefinition",
                                    "-XX:HotswapAgent=external",
                                    String.format(
                                            "-javaagent:%s",
                                            dcevmSpec
                                                    .getAgentJar()
                                                    .get()
                                                    .getAsFile()
                                                    .getAbsolutePath())));
                }));

        populateRegistry(
                JavaVersion.VERSION_21,
                () -> Collections.singletonList(dcevmSpec -> {
                    dcevmSpec.getLanguageVersion().set(JavaLanguageVersion.of(21));
                    dcevmSpec.getVendor().set(JvmVendorSpec.JETBRAINS);
                    dcevmSpec
                            .getArguments()
                            .convention(Arrays.asList(
                                    "-XX:+AllowEnhancedClassRedefinition",
                                    "-XX:HotswapAgent=external",
                                    String.format(
                                            "-javaagent:%s",
                                            dcevmSpec
                                                    .getAgentJar()
                                                    .get()
                                                    .getAsFile()
                                                    .getAbsolutePath())));
                }));
    }

    void populateRegistry(JavaVersion javaVersion, KnownDcevmSupplier knownDCEVMSupplier);

    void configureRegistry(Consumer<Map<JavaVersion, List<Action<? super DcevmSpec>>>> configureRegistry);
}
