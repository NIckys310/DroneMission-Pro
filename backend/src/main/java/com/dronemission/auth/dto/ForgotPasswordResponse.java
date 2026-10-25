package com.dronemission.auth.dto;

/**
 * Respuesta de la recuperación de contraseña SIMULADA.
 * En un sistema real el token se enviaría por correo; aquí se devuelve para la demo.
 */
public record ForgotPasswordResponse(String message, String simulatedToken) {
}
