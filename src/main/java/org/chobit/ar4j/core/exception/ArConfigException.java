package org.chobit.ar4j.core.exception;

public class ArConfigException extends RuntimeException {

    public ArConfigException(Throwable cause) {
        super(cause);
    }

    public ArConfigException(String message) {
        super(message);
    }

    public ArConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
