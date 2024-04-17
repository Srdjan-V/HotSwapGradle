package io.github.srdjanv.hotswapgradle.validator;

import org.gradle.api.Project;

import java.nio.file.Path;

@FunctionalInterface
public interface DcevmValidator {
    boolean validateDcevm(Path javaHome);
}
