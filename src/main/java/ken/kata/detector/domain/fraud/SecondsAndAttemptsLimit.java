package ken.kata.detector.domain.fraud;

public class SecondsAndAttemptsLimit {
    public final int seconds;
    public final int attempts;

    public SecondsAndAttemptsLimit(int seconds, int attempts) {
        this.seconds = seconds;
        this.attempts = attempts;
    }
}
