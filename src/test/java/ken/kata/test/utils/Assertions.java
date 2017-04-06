package ken.kata.test.utils;


public class Assertions {

    private Runnable runnable;
    private Exception exception;

    public Assertions(Runnable runnable) {
        this.runnable = runnable;
    }

    public static Assertions assertThat(Runnable runnable) {
        return new Assertions(runnable);
    }

    public Assertions throwsException(Class<? extends Exception> type) {
        try {
            runnable.run();
        } catch (Exception e) {
            exception = e;
            if (e.getClass().isAssignableFrom(type)) {
                return this;
            }
            throw new AssertionError("Expected exception " + type.getName() + " , but thrown " + e.getClass().getName());
        }
        throw new AssertionError("Expected to throw an exception " + type.getName() + ", but did not");

    }

    public Assertions withMessageContaining(String s) {
        if (exception.getMessage().contains(s)) {
            return this;
        }
        throw new AssertionError("Expected exception with message containing " + s + ", but was: " + exception.getMessage());
    }

}