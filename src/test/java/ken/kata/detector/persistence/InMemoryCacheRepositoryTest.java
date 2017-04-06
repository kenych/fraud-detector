package ken.kata.detector.persistence;

import ken.kata.detector.domain.FailedLogin;
import ken.kata.detector.service.scheduler.Scheduler;
import ken.kata.test.Samples;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


public class InMemoryCacheRepositoryTest {

    private Repository repository;
    private ConcurrentMap<String, FailedLogin> ipFailuresMap;

    private CountDownLatch countDownLatchOtherThread = new CountDownLatch(1);

    @Mock
    Scheduler scheduler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        ipFailuresMap = spy(new ConcurrentHashMap<>());
        repository = new InMemoryCacheRepository(ipFailuresMap, scheduler);
    }

    @Test
    public void addsNewWhenNotFound() {
        FailedLogin failedLogin = repository.logAndGet(Samples.FAILED_IP);

        assertThat(failedLogin.getLastFailTime()).isEqualTo(Samples.FAILED_IP.dateTime);
        assertThat(failedLogin.ip).isEqualTo(Samples.FAILED_IP.ip);
        verify(ipFailuresMap, times(1)).putIfAbsent(anyString(), anyObject());
        verify(ipFailuresMap, times(1)).get(anyString());
    }

    @Test
    public void updatesWhenFound() {
        givenFailedLoginExists();

        FailedLogin failedLogin = repository.logAndGet(Samples.FAILED_IP);

        assertThat(failedLogin.getLastFailTime()).isEqualTo(Samples.FAILED_IP.dateTime);
        assertThat(failedLogin.ip).isEqualTo(Samples.FAILED_IP.ip);
        verify(ipFailuresMap, never()).putIfAbsent(anyString(), anyObject());
        verify(ipFailuresMap, times(1)).get(anyString());
    }

    private void givenFailedLoginExists() {
        ipFailuresMap.put(Samples.FAILED_IP.ip, Samples.FAILED_LOGIN);
    }

    @Test
    public void raceConditionWhenUpdateDoesNotHappenAtFirstTryAsOtherThreadUpdatingFailedLogin() {
        givenFailedLoginExists();
        givenFailedLoginBeingUpdatedByOtherThread();

        FailedLogin failedLogin = repository.logAndGet(Samples.FAILED_IP);

        assertThat(failedLogin.getLastFailTime()).isEqualTo(Samples.FAILED_IP.dateTime);
        assertThat(failedLogin.ip).isEqualTo(Samples.FAILED_IP.ip);
        verify(ipFailuresMap, never()).putIfAbsent(anyString(), anyObject());
        verify(ipFailuresMap, atLeast(2)).get(anyString());
    }

    private void sleep(int delayInMillis) {
        try {
            Thread.sleep(delayInMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void givenFailedLoginBeingUpdatedByOtherThread()  {
        new Thread(() -> {
            //simulate failed login being updated by other thread
            Samples.FAILED_LOGIN.getLock().lock();
            try {
                //let caller thread proceed
                signalOtherThreadAlreadyStarted();

                //wait for called to execute and fail to lock acquisition and try again
                sleep(20);
            } finally {
                Samples.FAILED_LOGIN.getLock().unlock();
            }
        }).start();

        //make sure run after other thread has started
        waitForOtherThread();
    }



    private void signalOtherThreadAlreadyStarted() {
        countDownLatchOtherThread.countDown();
    }

    private void waitForOtherThread()  {
        try {
            countDownLatchOtherThread.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
