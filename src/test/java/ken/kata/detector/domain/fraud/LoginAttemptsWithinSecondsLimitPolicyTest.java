package ken.kata.detector.domain.fraud;

import ken.kata.detector.domain.FailedLogin;
import ken.kata.test.Samples;
import org.junit.Before;
import org.junit.Test;

import static java.util.stream.IntStream.rangeClosed;
import static org.fest.assertions.api.Assertions.assertThat;

public class LoginAttemptsWithinSecondsLimitPolicyTest {

    private FailedLogin failedLogin;
    private FraudPolicy fraudPolicy;

    @Before
    public void setUp() throws Exception {
        fraudPolicy = new LoginAttemptsWithinSecondsLimitPolicy(Samples.DEFAULT_LOGIN_ATTEMPTS_AND_TIME_LIMIT);
        failedLogin = new FailedLogin(Samples.NOW, Samples.GIVEN_IP);
    }

    @Test
    public void fraudIsNotDetectedWhenLessThanLimitAttempts() {
        rangeClosed(1, Samples.DEFAULT_ATTEMPTS_LIMIT - 2).forEach((i) -> failedLogin.addFailTime(Samples.NOW.minusMinutes(i)));

        assertThat(fraudPolicy.isFraudDetected(failedLogin)).isFalse();
    }

    @Test
    public void fraudIsNotDetectedWhenMoreThanLimitAttemptsInMoreThanLimitedTime() {
        rangeClosed(1, Samples.DEFAULT_ATTEMPTS_LIMIT * 2).forEach((i) -> failedLogin.addFailTime(Samples.NOW.minusMinutes(i * Samples.DEFAULT_MINUTE_LIMIT)));

        assertThat(fraudPolicy.isFraudDetected(failedLogin)).isFalse();
    }

    @Test
    public void fraudIsDetectedWhenMoreThanLimitAttemptsInLimitedTime() {
        rangeClosed(1, Samples.DEFAULT_ATTEMPTS_LIMIT * 2).forEach((i) -> failedLogin.addFailTime(Samples.NOW.minusSeconds(i)));

        assertThat(fraudPolicy.isFraudDetected(failedLogin)).isTrue();
    }
}
