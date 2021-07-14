package com.future94.swallow.common.exception;

/**
 * @author weilai
 */
public class SwallowException extends RuntimeException {

    public SwallowException() {
    }

    public SwallowException(String message) {
        super(message);
    }

    public SwallowException(String message, Throwable cause) {
        super(message, cause);
    }

    public SwallowException(Throwable cause) {
        super(cause);
    }

    public SwallowException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
