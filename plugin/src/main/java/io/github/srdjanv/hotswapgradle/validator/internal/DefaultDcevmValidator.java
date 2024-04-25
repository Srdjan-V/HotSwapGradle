package io.github.srdjanv.hotswapgradle.validator.internal;

import io.github.srdjanv.hotswapgradle.dcevmdetection.legacy.LegacyDcevmDetection;
import io.github.srdjanv.hotswapgradle.dcevmdetection.probe.ProbeDcevmDetection;
import io.github.srdjanv.hotswapgradle.validator.DcevmValidator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultDcevmValidator implements DcevmValidator {
    private final Logger logger = LoggerFactory.getLogger(DefaultDcevmValidator.class);
    private final Lock lock = new ReentrantLock();
    private final Map<Path, Boolean> cache = new HashMap<>();

    public DefaultDcevmValidator() {}

    @Override
    public boolean validateDcevm(Path javaHome) {
        lock.lock();
        try {
            return cache.computeIfAbsent(javaHome.toRealPath(), p -> {
                if (LegacyDcevmDetection.isPresent(p)) return true;
                var report = ProbeDcevmDetection.buildReport(p);
                if (report.vmMeta().isPresent()) return report.vmMeta().get().isDcevmPresent();
                return false;
            });
        } catch (IOException e) {
            logger.error("Error validating dcevm, path {}", javaHome, e);
            return false;
        } finally {
            lock.unlock();
        }
    }
}
