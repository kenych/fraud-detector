package ken.kata.detector.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FailedLogin {

    private List<LocalDateTime> failTimes;
    private LocalDateTime lastFailTime;
    public final String ip;

    private Lock lock = new ReentrantLock();

    public FailedLogin(LocalDateTime lastFailTime, String ip) {
        failTimes = new CopyOnWriteArrayList<>();
        failTimes.add(lastFailTime);
        this.lastFailTime = lastFailTime;
        this.ip = ip;
    }

    public void addFailTime(LocalDateTime failTime) {
        failTimes.add(failTime);

        if (failTime.isAfter(this.lastFailTime)) {
            this.lastFailTime = failTime;
        }
    }

    public Lock getLock() {
        return lock;
    }

    public LocalDateTime getLastFailTime() {
        return lastFailTime;
    }

    public List<LocalDateTime> getFailTimes() {
        return failTimes;
    }

    @Override
    public String toString() {
        return "FailedLogin{" +
                "ip='" + ip + '\'' +
                ", lastFailTime=" + lastFailTime +
                ", failTimes=" + failTimes +
                '}';
    }

    public boolean isExpired(LocalDateTime expirationTime) {
        return lastFailTime.equals(expirationTime);
    }
}
