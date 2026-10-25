package com.dronemission.common.exception;

/** Se lanza cuando se intenta registrar un valor que debe ser único (HTTP 409). */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
