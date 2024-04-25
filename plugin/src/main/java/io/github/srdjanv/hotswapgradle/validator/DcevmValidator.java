package io.github.srdjanv.hotswapgradle.validator;

import io.github.srdjanv.hotswapgradle.validator.internal.DefaultDcevmValidator;

import java.nio.file.Path;

@FunctionalInterface
public interface DcevmValidator {
    static DcevmValidator defaultValidator() {
        return new DefaultDcevmValidator();
    }

    boolean validateDcevm(Path javaHome);
}
