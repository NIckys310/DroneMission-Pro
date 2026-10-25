# CU-001 — Iniciar sesión

| | |
|---|---|
| **Actor** | Todos los usuarios |
| **Precondición** | El usuario está registrado. |
| **Requerimientos** | RF-001, RF-002, RF-003 |
| **Postcondición** | Sesión iniciada. |

## Flujo principal

1. Ingresa su correo.
2. Ingresa su contraseña.
3. El sistema valida las credenciales.
4. El sistema genera el token JWT.
5. El sistema redirige al inicio según su rol.

## Flujos alternativos

- 3a. Contraseña incorrecta: mensaje genérico y auditoría LOGIN_FALLIDO.
- 3b. Usuario inexistente: mismo mensaje genérico.
- 3c. Usuario inactivo: 'El usuario está inactivo'.
- 1a. Olvidó la contraseña: recuperación simulada con token temporal.
