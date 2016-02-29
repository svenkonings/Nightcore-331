package net.ddns.meteoorkip.nightcore_331.async;

public class TaskResult<T> {
    private final T value;
    private final Exception exception;

    public TaskResult(T value) {
        this(value, null);
    }

    public TaskResult(Exception exception) {
        this(null, exception);
    }

    public TaskResult(T value, Exception exception) {
        this.value = value;
        this.exception = exception;
    }

    public T getValue() {
        return value;
    }

    public boolean hasValue() {
        return value != null;
    }

    public Exception getException() {
        return exception;
    }

    public boolean hasException() {
        return exception != null;
    }
}
