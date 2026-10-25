# CU-003 — Gestionar drones

| | |
|---|---|
| **Actor** | Administrador (consulta: Operador, Técnico) |
| **Precondición** | Permiso DRONES_GESTIONAR. |
| **Requerimientos** | RF-006, RF-007, RF-008 |
| **Postcondición** | Flota actualizada. |

## Flujo principal

1. Abre Drones.
2. Busca o filtra.
3. Crea o edita un drone.
4. El Factory completa los valores del tipo.
5. El sistema guarda y audita.

## Flujos alternativos

- 4a. Serie duplicada: 409.
- 3a. Desactivar con misiones abiertas: rechazado.
