package io.github.srdjanv.hotswapgradle.dcevmdetection;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record VMMeta(boolean isDcevmPresent, String dcevmVersion) {}
