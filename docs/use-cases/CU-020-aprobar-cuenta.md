# CU-020 — Aprobar cuenta

| | |
|---|---|
| **Actor** | Administrador |
| **Precondición** | Hay cuentas pendientes. |
| **Requerimientos** | RF-032, RF-033 |
| **Postcondición** | La persona puede iniciar sesión con su rol. |

## Flujo principal

1. Recibe la notificación 'Nueva cuenta por aprobar'.
2. Abre Usuarios.
3. En 'Cuentas pendientes' pulsa Aprobar.
4. Elige el rol y, si aplica, la ficha de piloto o cliente.
5. El sistema activa la cuenta.

## Flujos alternativos

- 3a. Pulsa Rechazar: la cuenta se elimina.
- La cuenta de un usuario se bloquea por intentos fallidos: el administrador le asigna una nueva contraseña para desbloquearla.
