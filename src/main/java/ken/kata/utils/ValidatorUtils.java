package ken.kata.utils;

public class ValidatorUtils {
    public static void validateNotEmpty(String argument, String message) {
        if (argument == null || argument.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
