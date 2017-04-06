package ken.kata.test;

import ken.kata.detector.domain.FailedIp;
import ken.kata.detector.domain.FailedLogin;
import ken.kata.detector.domain.fraud.SecondsAndAttemptsLimit;

import java.time.LocalDateTime;

import static java.time.ZoneOffset.UTC;

public class Samples {
    public static final String GIVEN_IP = "80.238.9.179";
    public static final long DATE_TIME = LocalDateTime.now().toEpochSecond(UTC);
    public static final String SIGNIN_SUCCESS = "SIGNIN_SUCCESS";
    public static final String SIGNIN_FAILURE = "SIGNIN_FAILURE";
    public static final String USERNAME = "Dave.Branning";
    public static final String FAILED_LOG_LINE  = GIVEN_IP + "," + DATE_TIME + "," + SIGNIN_FAILURE + "," + USERNAME;
    public static final String SUCCESSFUL_LOG_LINE  = GIVEN_IP + "," + DATE_TIME + "," + SIGNIN_SUCCESS + "," + USERNAME;

    public static final LocalDateTime NOW = LocalDateTime.now();
    public static final LocalDateTime PAST_TIME = NOW.minusMinutes(1);
    public static final LocalDateTime FUTURE_TIME = NOW.plusMinutes(1);
    public static final int DEFAULT_MINUTE_LIMIT = 5;
    public static final int DEFAULT_ATTEMPTS_LIMIT = 5;
    public static final SecondsAndAttemptsLimit DEFAULT_LOGIN_ATTEMPTS_AND_TIME_LIMIT = new SecondsAndAttemptsLimit(DEFAULT_MINUTE_LIMIT, DEFAULT_ATTEMPTS_LIMIT);

    public static final int NEGATIVE_DELAY_IN_SECONDS = -3;
    public static final int DELAY_IN_SECONDS_2 = 2;

    public static final FailedIp FAILED_IP = new FailedIp(GIVEN_IP, NOW);
    public static final FailedLogin FAILED_LOGIN = new FailedLogin(NOW, GIVEN_IP);



}
