package io.github.srdjanv.hotswapgradle.dcevmdetection.probe;

import static io.github.srdjanv.hotswapgradle.dcevmdetection.probe.PlatformJavaPaths.resolveExecutable;
import static io.github.srdjanv.hotswapgradle.dcevmdetection.probe.ProbeBuilder.MARKER_PREFIX;

import io.github.srdjanv.hotswapgradle.dcevmdetection.VMMeta;
import io.github.srdjanv.hotswapgradle.dcevmdetection.VMReport;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProbeDcevmDetection {
    public static VMReport buildReport(Path javaHome) {
        try {
            Path tempDir = Files.createTempDirectory("HotswapProbe");
            tempDir.toFile().deleteOnExit();
            Path tempFile = tempDir.resolve("JavaProbe.class");
            ProbeBuilder.writeClass(tempFile);

            Process process = new ProcessBuilder(
                            resolveExecutable(javaHome).toAbsolutePath().toString(),
                            "-Xmx32m",
                            "-Xms32m",
                            "-cp",
                            tempDir.toRealPath().toAbsolutePath().toString(),
                            com.google.common.io.Files.getNameWithoutExtension(tempFile.toString()))
                    .start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (process.waitFor(3, TimeUnit.MINUTES)) {
                int exitValue = process.exitValue();
                if (exitValue == 0) return parseExecOutput(reader.lines());

                throw new Exception("Command returned unexpected result code: "
                        + exitValue + "\nError output:\n"
                        + reader.lines().collect(Collectors.joining(System.lineSeparator())));
            } else throw new Exception("VM Timeout reached");
        } catch (Exception exception) {
            return VMReport.exception(exception);
        }
    }

    public static VMReport parseExecOutput(Stream<String> out) {
        String[] split = out.filter(line -> line.startsWith(MARKER_PREFIX))
                .map(line -> line.substring(MARKER_PREFIX.length()))
                .toArray(String[]::new);
        if (split.length != Props.values().length)
            return VMReport.exception(new Exception("Invalid output format: " + Arrays.toString(split)));

        // should be fine to assume all JetBrains vms are dcevm
        var vmNameMatch = split[Props.VM_NAME.ordinal()].contains("Dynamic Code Evolution");
        var vmVendorMatch = split[Props.VM_VENDOR.ordinal()].contains("JetBrains");
        String dcevmVersion = split[Props.VM_VERSION.ordinal()];
        return VMReport.of(new VMMeta(vmNameMatch || vmVendorMatch, dcevmVersion));
    }
}
