package ken.kata.detector.domain;

import ken.kata.test.Samples;
import org.junit.Before;
import org.junit.Test;

import static java.time.LocalDateTime.now;
import static org.fest.assertions.api.Assertions.assertThat;

public class FailedLoginTest {

    private FailedLogin failedLogin;

    @Before
    public void setUp() throws Exception {
        failedLogin = new FailedLogin(Samples.NOW, Samples.GIVEN_IP);
    }

    @Test
    public void whenAddPastTimeThenDoesNotUpdateLastFailureTime() {
        failedLogin.addFailTime(Samples.PAST_TIME);

        assertThat(failedLogin.getLastFailTime()).isNotEqualTo(Samples.PAST_TIME);
    }

    @Test
    public void whenAddFutureTimeUpdateLastFailureTime() {
        failedLogin.addFailTime(Samples.FUTURE_TIME);

        assertThat(failedLogin.getLastFailTime()).isEqualTo(Samples.FUTURE_TIME);
    }

    @Test
    public void isNotExpiredWhenLastFailTimeIsResetByNewerLog() {
        boolean actualResult = failedLogin.isExpired(now().minusMinutes(1));

        assertThat(actualResult).isFalse();
    }

    @Test
    public void isExpiredWhenLastFailTimeIsSameAsExpirationTime() {
        boolean actualResult = failedLogin.isExpired(Samples.NOW);

        assertThat(actualResult).isTrue();
    }
}
