package ken.kata.detector.domain;

import ken.kata.test.utils.Assertions;
import ken.kata.test.Samples;
import org.junit.Test;

import static ken.kata.detector.domain.Action.SIGNIN_FAILURE;
import static java.time.ZoneOffset.UTC;
import static org.fest.assertions.api.Assertions.*;

public class LoginTest {

    @Test
    public void testCreateFrom() {
        //given
        String logLine = Samples.GIVEN_IP + "," + Samples.DATE_TIME + "," + SIGNIN_FAILURE + "," + Samples.USERNAME;

        //when
        Login login = Login.createFrom(logLine);

        //then
        assertThat(login.ip).isEqualTo(Samples.GIVEN_IP);
        assertThat(login.time.toEpochSecond(UTC)).isEqualTo(Samples.DATE_TIME);
        assertThat(login.signInAction).isEqualTo(SIGNIN_FAILURE);
        assertThat(login.userName).isEqualTo(Samples.USERNAME);
    }

    @Test
    public void testCreateFailNullIp() {
        String logLine = "" + "," + Samples.DATE_TIME + "," + SIGNIN_FAILURE + "," + Samples.USERNAME;

        Assertions.assertThat(() -> Login.createFrom(logLine))
                .throwsException(IllegalArgumentException.class)
                .withMessageContaining("ip");

    }

    @Test
    public void testCreateFailWrongFormat() {
        String logLine = "";

        Assertions.assertThat(() -> Login.createFrom(logLine))
                .throwsException(IllegalArgumentException.class)
                .withMessageContaining("format");

    }
}
