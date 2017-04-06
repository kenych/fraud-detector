package ken.kata.detector.domain.fraud;

import ken.kata.detector.domain.FailedLogin;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.Collections.sort;

public class LoginAttemptsWithinSecondsLimitPolicy implements FraudPolicy {

    private SecondsAndAttemptsLimit secondsAndAttemptsLimit;

    public LoginAttemptsWithinSecondsLimitPolicy(SecondsAndAttemptsLimit secondsAndAttemptsLimit) {
        this.secondsAndAttemptsLimit = secondsAndAttemptsLimit;
    }

    @Override
    public boolean isFraudDetected(FailedLogin failedLogin) {

        if (failedLogin.getFailTimes().size() < secondsAndAttemptsLimit.attempts) {
            return false;
        }

        //todo potential bug fix later, as it is a shallow copy only!
        List<LocalDateTime> failTimesCopy = new ArrayList<>(failedLogin.getFailTimes());
        sort(failTimesCopy, (o1, o2) -> o2.compareTo(o1));

        LocalDateTime limitTime = now().minusSeconds(secondsAndAttemptsLimit.seconds);
        LocalDateTime earliestFailTime = failTimesCopy.get(secondsAndAttemptsLimit.attempts - 1);
        return !earliestFailTime.isBefore(limitTime);
    }

}
