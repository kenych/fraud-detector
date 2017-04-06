package ken.kata.detector.domain.fraud;

import ken.kata.detector.domain.FailedLogin;

public interface FraudPolicy {
    boolean isFraudDetected(FailedLogin failedLogin);
}
