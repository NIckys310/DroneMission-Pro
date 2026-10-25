# CU-004 — Gestionar pilotos

| | |
|---|---|
| **Actor** | Administrador (consulta: Operador) |
| **Precondición** | Permiso PILOTOS_GESTIONAR. |
| **Requerimientos** | RF-010, RF-011 |
| **Postcondición** | Piloto registrado. |

## Flujo principal

1. Abre Pilotos.
2. Crea o edita el piloto.
3. Registra su licencia y certificaciones.
4. El sistema guarda.

## Flujos alternativos

- 3a. Licencia vencida: el piloto queda LICENCIA_VENCIDA y no es asignable.
- 4a. Licencia duplicada: 409.
