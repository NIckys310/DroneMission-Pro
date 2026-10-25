package com.dronemission.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato único de error que devuelve la API.
 * Ejemplo: {"timestamp": "...", "status": 400, "error": "Bad Request",
 *           "message": "El drone seleccionado no está disponible", "path": "/api/missions/15/assign"}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(LocalDateTime.now(), status, error, message, path, null);
    }
}
