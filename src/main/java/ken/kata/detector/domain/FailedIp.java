package ken.kata.detector.domain;


import java.time.LocalDateTime;

public class FailedIp {
    public final String ip;
    public final LocalDateTime dateTime;

    public FailedIp(String ip, LocalDateTime dateTime) {
        this.ip = ip;
        this.dateTime = dateTime;
    }
}
