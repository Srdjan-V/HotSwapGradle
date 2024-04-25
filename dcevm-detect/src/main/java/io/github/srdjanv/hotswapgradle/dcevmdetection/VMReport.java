package io.github.srdjanv.hotswapgradle.dcevmdetection;

import com.github.bsideup.jabel.Desugar;
import java.util.Optional;

@Desugar
public record VMReport(Optional<VMMeta> vmMeta, Optional<Exception> exception) {
    public static VMReport none() {
        return new VMReport(Optional.empty(), Optional.empty());
    }

    public static VMReport none(String message) {
        return new VMReport(Optional.empty(), Optional.of(new Exception(message)));
    }

    public static VMReport exception(Exception exception) {
        return new VMReport(Optional.empty(), Optional.of(exception));
    }

    public static VMReport of(VMMeta meta) {
        return new VMReport(Optional.of(meta), Optional.empty());
    }
}
