package ken.kata.detector.service;

import java.util.Optional;

public interface FraudDetector {
    public Optional<String> detectFraud(String logLine);
}
