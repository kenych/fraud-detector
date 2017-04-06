package ken.kata.utils;

import java.util.concurrent.locks.Lock;

public class LockUtils {
    public static boolean runInLock(Runnable runnable, Lock lock) {
        boolean lockAcquired = lock.tryLock();
        if (lockAcquired) {
            try {
                runnable.run();
            } finally {
                lock.unlock();
            }
        }
        return lockAcquired;
    }
}
