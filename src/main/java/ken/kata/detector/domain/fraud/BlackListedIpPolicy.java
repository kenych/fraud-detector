package ken.kata.detector.domain.fraud;

import ken.kata.detector.domain.FailedLogin;

import java.util.Set;

/**
 * This class is not used and just an example of how Fraud Policies can be expanded
 */
public class BlackListedIpPolicy implements FraudPolicy {

    private Set<String> blackListedIps;

    public BlackListedIpPolicy(Set<String> blackListedIps) {
        this.blackListedIps = blackListedIps;
    }

    @Override
    public boolean isFraudDetected(FailedLogin failedLogin) {
        return blackListedIps.contains(failedLogin.ip);
    }
}
