package ken.kata.detector.service;

import ken.kata.detector.domain.Action;
import ken.kata.detector.domain.FailedLogin;
import ken.kata.detector.domain.fraud.LoginAttemptsWithinSecondsLimitPolicy;
import ken.kata.detector.domain.fraud.SecondsAndAttemptsLimit;
import ken.kata.detector.persistence.InMemoryCacheRepository;
import ken.kata.detector.service.scheduler.CleanUpScheduler;
import ken.kata.detector.service.scheduler.DelayedCleanUpJob;
import ken.kata.test.Samples;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.*;
import java.util.logging.Logger;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.IntStream.rangeClosed;
import static org.fest.assertions.api.Assertions.assertThat;

public class DemoTest {

    int CLEAN_UP_DELAY_IN_SECONDS = 6;
    int LIMIT_SECONDS = 5;
    int LIMIT_ATTEMPTS = 3;
    int MAX_TIMES = 5;
    int SLEEP_TIME_PER_IP = 1000;
    int SLEEP_TIME_PER_THREAD = 50;
    int THREADS = 20;

    Logger logger = Logger.getLogger(DemoTest.class.getName());
    Random random = new Random();

    FraudDetector fraudDetector;
    BlockingQueue<DelayedCleanUpJob> queue;
    ConcurrentMap<String, FailedLogin> ipFailuresMap;

    @Before
    public void setUp() throws Exception {

        ipFailuresMap = new ConcurrentHashMap<>();

        queue = new DelayQueue<>();

        CleanUpScheduler cleanUpScheduler = new CleanUpScheduler(queue, ipFailuresMap, CLEAN_UP_DELAY_IN_SECONDS);

        cleanUpScheduler.start();

        InMemoryCacheRepository repository = new InMemoryCacheRepository(ipFailuresMap, cleanUpScheduler);

        fraudDetector = new FraudDetectorImpl(
                repository,
                asList(new LoginAttemptsWithinSecondsLimitPolicy(new SecondsAndAttemptsLimit(LIMIT_SECONDS, LIMIT_ATTEMPTS))));
    }


    @Test
    @Ignore("it is not a good idea to have this delayed test in CI, run manually only")
    /**
     * This test is for demo. Run and watch for the logs. Log level(JUL is used) deliberately set to INFO in most places, whereas
     * in production it would of course be FINE and configured through the properties.
     *
     * It creates 20 threads with pseudo different IP and then every thread sends parseLine request every second for 5 times.
     *
     * In fact generated IP could be same as the one already created by other thread and this can be controlled through randomNum
     * by just updating it by higher values to eliminate chances of duplication. On the other hand it is nice to see what
     * happens when IPs clash, to demonstrate no dead lock or any other locking issues exist.
     *
     * Upon 3rd request, if all IPs are unique, parseLine should detect fraud IP as it configured by SecondsAndAttemptsLimit.
     *
     * Every failed attempt creates a clean up job which is executed upon expiration and should delete IP from cache
     * if there were no updates for this IP after job has been created.
     * Eventually all IPs should be deleted from the cache once stopped being updated, thus assuring no memory leakage.
     *
     */
    public void demo() throws InterruptedException {

        ExecutorService executor = newFixedThreadPool(THREADS);
        CountDownLatch countDownLatch = new CountDownLatch(THREADS);

        for (int i = 0; i < THREADS; i++) {
            String ipPerThread = generateIp();
            executor.execute(() -> {
                rangeClosed(1, MAX_TIMES).forEach((e) -> {
                    fraudDetector.detectFraud(ipPerThread + "," + LocalDateTime.now().toEpochSecond(UTC) + "," + Action.values()[random.nextInt(2)] + "," + Samples.USERNAME);
                    countDownLatch.countDown();
                    //wait until next call by IP
                    sleep(SLEEP_TIME_PER_IP);
                });
            });
            //wait until next thread execution
            sleep(SLEEP_TIME_PER_THREAD);

        }

        countDownLatch.await();

        while (queue.size() > 0) {
            logger.info("waiting for clean up queue: " + queue.size());
            sleep(1000);
        }

        //assert memory is cleaned up
        assertThat(queue.size()).isEqualTo(0);
        assertThat(ipFailuresMap.size()).isEqualTo(0);
    }

    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String generateIp() {
        return randomNum() + "." + randomNum() + "." + randomNum() + "." + randomNum();
    }

    private int randomNum() {
        return random.nextInt(2) + 1;
    }

}
