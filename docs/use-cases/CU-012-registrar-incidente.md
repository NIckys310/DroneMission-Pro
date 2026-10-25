# CU-012 — Registrar incidente

| | |
|---|---|
| **Actor** | Piloto (gestión: Operador) |
| **Precondición** | Misión iniciada o programada. |
| **Requerimientos** | RF-023, RF-024 |
| **Postcondición** | Incidente registrado. |

## Flujo principal

1. Pulsa Incidente.
2. Selecciona tipo y severidad.
3. Describe lo ocurrido.
4. El sistema guarda el incidente ABIERTO.

## Flujos alternativos

- 2a. Severidad CRÍTICA: notifica a operaciones y pausa la misión.
- El operador lo pasa a EN_REVISIÓN / RESUELTO con la resolución.
