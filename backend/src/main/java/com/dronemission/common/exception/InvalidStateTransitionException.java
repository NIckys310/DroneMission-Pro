package com.dronemission.common.exception;

/** Se lanza cuando se intenta un cambio de estado no permitido (HTTP 409). */
public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
