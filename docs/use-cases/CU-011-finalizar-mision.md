# CU-011 — Finalizar misión

| | |
|---|---|
| **Actor** | Piloto |
| **Precondición** | Misión EN_VUELO o PAUSADA. |
| **Requerimientos** | RF-019, RF-030 |
| **Postcondición** | Misión FINALIZADA con reporte. |

## Flujo principal

1. Pulsa Finalizar y escribe observaciones.
2. El sistema resume la telemetría.
3. Suma horas de vuelo al drone y al piloto.
4. Libera los recursos.
5. Genera el reporte y notifica al cliente.

## Flujos alternativos

- Misión ya finalizada: no se puede reabrir.
