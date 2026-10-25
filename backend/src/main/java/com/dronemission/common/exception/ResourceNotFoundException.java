package com.dronemission.common.exception;

/** Se lanza cuando un recurso solicitado no existe (HTTP 404). */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " con id " + id + " no existe");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
