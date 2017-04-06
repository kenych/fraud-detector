package ken.kata.detector.service;

import ken.kata.detector.domain.FailedIp;
import ken.kata.detector.domain.FailedLogin;
import ken.kata.detector.domain.Login;
import ken.kata.detector.domain.fraud.FraudPolicy;
import ken.kata.detector.persistence.Repository;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class FraudDetectorImpl implements FraudDetector {

    private Repository repository;
    private List<? extends FraudPolicy> fraudPolicies;

    public FraudDetectorImpl(Repository repository, List<? extends FraudPolicy> fraudPolicies) {
        this.repository = repository;
        this.fraudPolicies = fraudPolicies;
    }

    @Override
    public Optional<String> detectFraud(String logLine) {
        Login login = Login.createFrom(logLine);

        if(login.isSuccessful()){
            return empty();
        }

        final FailedLogin failedLogin = repository.logAndGet(new FailedIp(login.ip, login.time));

        return login.isSuccessful() ?
                empty() :
                fraudPolicies.stream().anyMatch(policy -> policy.isFraudDetected(failedLogin)) ?
                        of(login.ip) :
                        empty();

    }
}