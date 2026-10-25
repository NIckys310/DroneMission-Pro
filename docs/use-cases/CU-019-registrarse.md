# CU-019 — Registrarse

| | |
|---|---|
| **Actor** | Visitante |
| **Precondición** | El registro está habilitado. |
| **Requerimientos** | RF-031 |
| **Postcondición** | Cuenta pendiente. |

## Flujo principal

1. Pulsa 'Crear cuenta' en el login.
2. Escribe nombre, apellidos, usuario, correo y contraseña (la pantalla muestra en vivo qué requisitos cumple).
3. Confirma la contraseña.
4. El sistema valida y crea la cuenta pendiente.
5. Muestra: 'Un administrador debe aprobarla'.

## Flujos alternativos

- 3a. Contraseña débil: no se envía hasta cumplir los requisitos; el backend también la rechaza.
- 4a. Usuario o correo ya registrados: error 409.
- Intenta entrar antes de ser aprobado: 'Tu cuenta está pendiente de aprobación'.
