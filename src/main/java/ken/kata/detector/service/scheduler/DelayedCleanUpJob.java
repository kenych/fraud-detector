package ken.kata.detector.service.scheduler;

import ken.kata.detector.domain.FailedIp;
import ken.kata.detector.domain.FailedLogin;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static ken.kata.utils.LockUtils.runInLock;
import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;

public class DelayedCleanUpJob implements Delayed, Job {

    private final static Logger logger = Logger.getLogger(DelayedCleanUpJob.class.getName());

    private long delayInSeconds;
    private long createdTimeInSeconds;
    private FailedIp failedIp;
    private ConcurrentMap<String, FailedLogin> ipFailuresMap;

    public DelayedCleanUpJob(FailedIp failedIp, ConcurrentMap ipFailuresMap, long delayInSeconds) {
        this.failedIp = failedIp;
        this.ipFailuresMap = ipFailuresMap;
        this.delayInSeconds = delayInSeconds;
        this.createdTimeInSeconds = nowInSeconds();
    }

    private long nowInSeconds() {
        return now().toEpochSecond(UTC);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return delayInSeconds - (nowInSeconds() - createdTimeInSeconds);
    }

    @Override
    public void execute() {
        logger.info("IP cleanup: " + failedIp.ip + " " + failedIp.dateTime);

        FailedLogin failedLogin = ipFailuresMap.get(failedIp.ip);

        if (failedLogin != null) {
            if (failedLogin.isExpired(failedIp.dateTime)) {
                boolean success = runInLock(() -> ipFailuresMap.remove(failedIp.ip), failedLogin.getLock());
                if (success) {
                    logger.info("failedLogin deleted: " + failedLogin);
                } else {
                    logger.info("can't delete, failedLogin is being updated: " + failedLogin);
                }
            } else {
                logger.info("can't delete " + failedLogin + " is not expired at " + failedIp.dateTime);
            }
        } else {
            logger.info("can't delete, IP not found: " + failedIp.ip);
        }

    }

    @Override
    public int compareTo(Delayed delayed) {
        Long otherDelay =  delayed.getDelay(null);
        Long thisDelay = getDelay(null);
        return thisDelay.compareTo(otherDelay);
    }
}
