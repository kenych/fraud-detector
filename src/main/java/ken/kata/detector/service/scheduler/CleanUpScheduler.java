package ken.kata.detector.service.scheduler;

import ken.kata.detector.domain.FailedIp;
import ken.kata.detector.domain.FailedLogin;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class CleanUpScheduler extends Thread implements Scheduler {

    private int delayInSeconds;

    private final static Logger logger = Logger.getLogger(CleanUpScheduler.class.getName());

    private BlockingQueue<DelayedCleanUpJob> cleanUpQueue;

    private ConcurrentMap<String, FailedLogin> ipFailuresMap;

    public CleanUpScheduler(BlockingQueue<DelayedCleanUpJob> cleanUpQueue, ConcurrentMap<String, FailedLogin> ipFailuresMap, int delayInSeconds) {
        this.cleanUpQueue = cleanUpQueue;
        this.ipFailuresMap = ipFailuresMap;
        this.delayInSeconds = delayInSeconds;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Job job = cleanUpQueue.take();
                job.execute();
            } catch (InterruptedException e) {
                logger.warning(e.getMessage());
            }
        }
    }

    @Override
    public void schedule(FailedIp failedIp) {
        try {
            cleanUpQueue.put(new DelayedCleanUpJob(failedIp, ipFailuresMap, delayInSeconds));
        } catch (InterruptedException e) {
            logger.warning(e.getMessage());
        }
    }

}
