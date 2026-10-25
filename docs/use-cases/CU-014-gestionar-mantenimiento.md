# CU-014 — Gestionar mantenimiento

| | |
|---|---|
| **Actor** | Técnico |
| **Precondición** | Permiso MANTENIMIENTO_GESTIONAR. |
| **Requerimientos** | RF-009, RF-026 |
| **Postcondición** | Historial de mantenimiento actualizado. |

## Flujo principal

1. Revisa alertas de mantenimiento.
2. Registra el mantenimiento.
3. Lo inicia: el drone pasa a EN_MANTENIMIENTO.
4. Lo completa con costo y próxima fecha: el drone vuelve a DISPONIBLE.

## Flujos alternativos

- 3a. Drone en misión: no se puede iniciar.
- Cancelar: el drone vuelve a DISPONIBLE.
