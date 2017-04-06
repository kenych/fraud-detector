package ken.kata.utils;

import ken.kata.test.utils.Assertions;
import org.junit.Test;

import static ken.kata.utils.ValidatorUtils.validateNotEmpty;

public class ValidatorUtilsTest {
    @Test
    public void validateNotEmptyWhenNull() throws Exception {
        Assertions.assertThat(() -> validateNotEmpty(null, "argument"))
                .throwsException(IllegalArgumentException.class)
                .withMessageContaining("argument");
    }

    @Test
    public void validateNotEmptyWhenEmpty() throws Exception {
        Assertions.assertThat(() -> validateNotEmpty("", "argument"))
                .throwsException(IllegalArgumentException.class)
                .withMessageContaining("argument");
    }
}
