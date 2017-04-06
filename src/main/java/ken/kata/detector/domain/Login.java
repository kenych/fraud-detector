package ken.kata.detector.domain;


import ken.kata.utils.ValidatorUtils;

import java.time.LocalDateTime;

import static java.time.ZoneOffset.UTC;

public class Login {
    public final String ip;
    public final LocalDateTime time;
    public final Action signInAction;
    public final String userName;

    public Login(String ip, LocalDateTime time, Action signInAction, String userName) {
        this.ip = ip;
        this.time = time;
        this.signInAction = signInAction;
        this.userName = userName;
    }

    public boolean isSuccessful() {
        return signInAction == Action.SIGNIN_SUCCESS;
    }

    public static Login createFrom(String logLine) {
        String[] lineParts = logLine.split(",");

        if (lineParts.length < 4) {
            throw new IllegalArgumentException("Log line is wrong format");
        }

        String ip = lineParts[0];
        String epochSecond = lineParts[1];
        String action = lineParts[2];
        String name = lineParts[3];

        ValidatorUtils.validateNotEmpty(ip, "ip");
        ValidatorUtils.validateNotEmpty(epochSecond, "epochSecond");
        ValidatorUtils.validateNotEmpty(action, "action");

        return new Login(
                ip,
                LocalDateTime.ofEpochSecond(Long.valueOf(epochSecond), 0, UTC),
                Action.valueOf(action),
                name);
    }


}
