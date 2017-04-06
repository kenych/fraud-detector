package ken.kata.detector.service.scheduler;

import ken.kata.detector.domain.FailedLogin;
import ken.kata.test.Samples;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Delayed;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


public class DelayedCleanUpJobTest {

    private CountDownLatch countDownLatchCleanUp = new CountDownLatch(1);
    private CountDownLatch countDownLatchUpdate = new CountDownLatch(1);
    private int DELAY_ZERO = 0;

    @Mock
    private ConcurrentMap<String, FailedLogin> ipFailuresMap;

    @Before
    public void setUp() {
        initMocks(this);
        when(ipFailuresMap.get(anyString())).thenReturn(Samples.FAILED_LOGIN);
    }

    @Test
    public void testDelayedArtificially() {
        Delayed delayedJob = new DelayedCleanUpJob(null, null, Samples.NEGATIVE_DELAY_IN_SECONDS);

        long actual = delayedJob.getDelay(null);

        assertThat(actual).isLessThan(0);
    }

    @Test
    @Ignore(value = "it is not a good idea to have this delayed demo in CI, run manually only")
    public void testDelayed() throws InterruptedException {
        Delayed delayedJob = new DelayedCleanUpJob(null, null, Samples.DELAY_IN_SECONDS_2);

        sleep(Samples.DELAY_IN_SECONDS_2);
        long actual = delayedJob.getDelay(null);

        assertThat(actual).isLessThanOrEqualTo(0);
    }

    @Test
    public void raceConditionWhenCleanUpThreadDoesNotRemoveIpAsFailedLoginIsBeingUpdatedByOtherThread() throws Exception {
        Job delayedJob = new DelayedCleanUpJob(Samples.FAILED_IP, ipFailuresMap, DELAY_ZERO);
        givenFailedLoginBeingUpdatedByOtherThread();

        delayedJob.execute();

        verify(ipFailuresMap, never()).remove(anyString());
        signalCleanUpThreadAlreadyStarted();
    }


    @Test
    public void executeRemovesExpiredIp() throws Exception {
        Job delayedJob = new DelayedCleanUpJob(Samples.FAILED_IP, ipFailuresMap, DELAY_ZERO);

        delayedJob.execute();

        verify(ipFailuresMap, atLeastOnce()).remove(anyString());
    }

    @Test
    public void executeDoesNotRemoveIpWhenExpirationIsReset() throws Exception {
        Job delayedJob = new DelayedCleanUpJob(Samples.FAILED_IP, ipFailuresMap, DELAY_ZERO);

        Samples.FAILED_LOGIN.addFailTime(Samples.NOW.plusMinutes(1));

        delayedJob.execute();

        verify(ipFailuresMap, never()).remove(anyString());
    }


    private void sleep(int delayInSeconds) {
        try {
            Thread.sleep(delayInSeconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void signalCleanUpThreadAlreadyStarted() {
        //signal update thread to release lock to let other threads(tests) proceed with lock acquisition
        countDownLatchCleanUp.countDown();

        //make sure thread releases lock as it might be still locked when calling other test
        sleep(1);
    }

    private void givenFailedLoginBeingUpdatedByOtherThread()  {
        new Thread(() -> {
            //simulate failed login being updated
            Samples.FAILED_LOGIN.getLock().lock();
            try {
                //let clean up thread proceed
                signalUpdateThreadAlreadyStarted();

                //wait for cleanup to execute and fail to lock acquisition then unlock and let it proceed
                waitForCleanUpThread();
            } finally {
                Samples.FAILED_LOGIN.getLock().unlock();
            }
        }).start();

        //make sure run after update thread has started
        waitForUpdateThread();
    }

    private void waitForCleanUpThread() {
        try {
            countDownLatchCleanUp.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void signalUpdateThreadAlreadyStarted() {
        countDownLatchUpdate.countDown();
    }

    private void waitForUpdateThread()  {
        try {
            countDownLatchUpdate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
