package com.dronemission.common.exception;

/** Credenciales inválidas o usuario inactivo (HTTP 401). */
public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
