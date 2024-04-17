package io.github.srdjanv.hotswapgradle.validator.internal;

import io.github.srdjanv.hotswapgradle.util.DCEVMUtil;
import io.github.srdjanv.hotswapgradle.validator.DcevmValidator;

import java.nio.file.Path;

public class DefaultDcevmValidator implements DcevmValidator {
    public DefaultDcevmValidator() {
    }

    @Override public boolean validateDcevm(Path javaHome) {
        return DCEVMUtil.isDCEVMPresent(javaHome);
    }
}
