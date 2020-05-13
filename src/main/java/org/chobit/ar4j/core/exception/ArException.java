package org.chobit.ar4j.core.exception;

public class ArException extends RuntimeException {

    public ArException(String message) {
        super(message);
    }


    public ArException(String message, Throwable cause) {
        super(message, cause);
    }
}
