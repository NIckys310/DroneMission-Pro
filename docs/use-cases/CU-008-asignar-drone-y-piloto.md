# CU-008 — Asignar drone y piloto

| | |
|---|---|
| **Actor** | Operador |
| **Precondición** | Misión PENDIENTE o PROGRAMADA. |
| **Requerimientos** | RF-016 |
| **Postcondición** | Misión PROGRAMADA. |

## Flujo principal

1. Abre 'Asignar recursos'.
2. El sistema lista drones y pilotos indicando elegibilidad y motivo.
3. Selecciona un drone y un piloto.
4. El sistema valida reglas 1-5.
5. Guarda la asignación y pasa a PROGRAMADA.
6. Notifica al piloto y al cliente.

## Flujos alternativos

- 4a. Drone en mantenimiento / fuera de servicio / sin batería / incompatible: rechazo con motivo.
- 4b. Piloto con licencia vencida o no disponible: rechazo.
- 4c. Cruce de horario: rechazo.
