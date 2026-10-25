# CU-002 — Gestionar usuarios y roles

| | |
|---|---|
| **Actor** | Administrador |
| **Precondición** | Sesión de administrador. |
| **Requerimientos** | RF-004, RF-005 |
| **Postcondición** | Usuario/rol actualizado y auditado. |

## Flujo principal

1. Abre Usuarios.
2. Crea/edita un usuario con su rol.
3. Vincula su ficha de piloto o cliente (opcional).
4. El sistema valida y guarda.
5. En Roles y permisos ajusta la matriz de permisos.

## Flujos alternativos

- 4a. Correo duplicado: error 409.
- 5a. Intenta editar el rol ADMINISTRADOR: no permitido.
