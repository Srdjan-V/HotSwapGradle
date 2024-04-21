package io.github.srdjanv.hotswapgradle.validator.internal;

import io.github.srdjanv.hotswapgradle.dcevmdetection.legacy.LegacyDcevmDetection;
import io.github.srdjanv.hotswapgradle.validator.DcevmValidator;
import java.nio.file.Path;

public class DefaultDcevmValidator implements DcevmValidator {
    public DefaultDcevmValidator() {}

    @Override
    public boolean validateDcevm(Path javaHome) {
        return LegacyDcevmDetection.isPresent(javaHome);
    }
}
