package ken.kata.detector.persistence;

import ken.kata.detector.domain.FailedIp;
import ken.kata.detector.domain.FailedLogin;
import ken.kata.detector.service.scheduler.Scheduler;

import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import static ken.kata.utils.LockUtils.runInLock;

public class InMemoryCacheRepository implements Repository {

    private final static Logger logger = Logger.getLogger(InMemoryCacheRepository.class.getName());

    private ConcurrentMap<String, FailedLogin> ipFailuresMap;

    private Scheduler cleanUpScheduler;

    public InMemoryCacheRepository(ConcurrentMap<String, FailedLogin> ipFailuresMap, Scheduler cleanUpScheduler) {
        this.ipFailuresMap = ipFailuresMap;
        this.cleanUpScheduler = cleanUpScheduler;
    }

    @Override
    public FailedLogin logAndGet(FailedIp failedIp) {

        FailedLogin failedLogin = addOrUpdate(failedIp);

        cleanUpScheduler.schedule(failedIp);

        return failedLogin;
    }

    private FailedLogin addOrUpdate(FailedIp failedIp) {
        FailedLogin foundFailedLogin = ipFailuresMap.get(failedIp.ip);

        //try to add if doesn't exist
        if (foundFailedLogin == null) {
            FailedLogin newFailedLogin = new FailedLogin(failedIp.dateTime, failedIp.ip);
            foundFailedLogin = ipFailuresMap.putIfAbsent(failedIp.ip, newFailedLogin);
            //turns out other thread have already added
            if (foundFailedLogin != null) {
                update(foundFailedLogin, failedIp);
            } else {
                logger.info("added: " + newFailedLogin);
                return newFailedLogin;
            }
        } else {
            update(foundFailedLogin, failedIp);
        }

        logger.info("updated: " + foundFailedLogin);
        return foundFailedLogin;
    }

    /**
     * Try to update, if lock can not be obtained due to clean up thread trying to delete expired IP
     * or other thread trying to update same IP, we try again
     */
    private void update(FailedLogin failedLogin, FailedIp failTime) {
        boolean successful = runInLock(() -> failedLogin.addFailTime(failTime.dateTime), failedLogin.getLock());

        if (!successful) {
            logger.info("can't update, failedLogin is being updated by other thread or deleted by clean up: " + failedLogin);
            addOrUpdate(failTime);
        }
    }


}

