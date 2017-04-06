package ken.kata.detector.service;

import ken.kata.detector.domain.FailedLogin;
import ken.kata.detector.domain.fraud.LoginAttemptsWithinSecondsLimitPolicy;
import ken.kata.detector.persistence.InMemoryCacheRepository;
import ken.kata.detector.service.scheduler.Scheduler;
import ken.kata.test.Samples;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.verification.VerificationMode;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Arrays.asList;
import static java.util.stream.IntStream.range;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

public class FraudDetectorImplTest {

    private FraudDetector fraudDetector;

    private InMemoryCacheRepository repository;

    private ConcurrentMap<String, FailedLogin> ipFailuresMap;

    @Mock
    private Scheduler scheduler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        ipFailuresMap = new ConcurrentHashMap<>();

        repository = spy(new InMemoryCacheRepository(ipFailuresMap, scheduler));

        fraudDetector = new FraudDetectorImpl(
                repository,
                asList(new LoginAttemptsWithinSecondsLimitPolicy(Samples.DEFAULT_LOGIN_ATTEMPTS_AND_TIME_LIMIT)));
    }

    @Test
    public void parseLineReturnsNullWhenOnlySuccessfulAttempts() throws Exception {
        String logLine = Samples.SUCCESSFUL_LOG_LINE;

        Optional<String> result = fraudDetector.detectFraud(logLine);

        assertThat(result.isPresent()).isFalse();

        verifyServicesAreCalled(never());
    }


    @Test
    public void parseLineReturnsNullWhenLessThanLimitAttempt() throws Exception {
        String logLine = Samples.FAILED_LOG_LINE;

        range(1, Samples.DEFAULT_ATTEMPTS_LIMIT - 1).forEach((i) -> fraudDetector.detectFraud(logLine));
        Optional<String> result = fraudDetector.detectFraud(logLine);

        assertThat(result.isPresent()).isFalse();

        verifyServicesAreCalled(atLeastOnce());
    }

    @Test
    public void parseLineReturnsIpWhenEqualsLimitAttempts() throws Exception {
        String logLine = Samples.FAILED_LOG_LINE;

        range(0, Samples.DEFAULT_ATTEMPTS_LIMIT).forEach((i) -> fraudDetector.detectFraud(logLine));
        Optional<String> result = fraudDetector.detectFraud(logLine);


        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(Samples.GIVEN_IP);

        verifyServicesAreCalled(atLeastOnce());
    }

    @Test
    public void parseLineReturnsIpWhenMoreThanLimitAttempts() throws Exception {
        String logLine = Samples.FAILED_LOG_LINE;

        range(0, Samples.DEFAULT_ATTEMPTS_LIMIT + 10).forEach((i) -> fraudDetector.detectFraud(logLine));
        Optional<String> result = fraudDetector.detectFraud(logLine);

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(Samples.GIVEN_IP);

        verifyServicesAreCalled(atLeastOnce());
    }

    private void verifyServicesAreCalled(VerificationMode verificationMode) {
        verify(repository, verificationMode).logAndGet(anyObject());
        verify(scheduler, verificationMode).schedule(anyObject());
    }
}
