package ken.kata.detector.service.scheduler;

import ken.kata.detector.domain.FailedIp;

public interface Scheduler {
    void schedule(FailedIp failedIp);
}
