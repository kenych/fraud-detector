package ken.kata.detector.persistence;

import ken.kata.detector.domain.FailedIp;
import ken.kata.detector.domain.FailedLogin;

public interface Repository {
    public FailedLogin logAndGet(FailedIp failedIp);
}
