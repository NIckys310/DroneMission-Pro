package com.dronemission.common.exception;

/**
 * Se lanza cuando una operación viola una regla de negocio (HTTP 400).
 * Ejemplo: asignar un drone que está en mantenimiento.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
