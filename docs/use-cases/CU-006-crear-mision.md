# CU-006 — Crear misión

| | |
|---|---|
| **Actor** | Operador |
| **Precondición** | Existe al menos un cliente activo. |
| **Requerimientos** | RF-013, RF-014 |
| **Postcondición** | Misión en BORRADOR con plan de vuelo. |

## Flujo principal

1. Abre Nueva misión.
2. Ingresa nombre, tipo, cliente, ubicación, coordenadas, altitud, fecha, horario, prioridad y objetivo.
3. El sistema valida (Builder).
4. Genera el plan de vuelo con la estrategia del tipo.
5. Guarda la misión en BORRADOR.

## Flujos alternativos

- 3a. Hora fin ≤ inicio, fecha pasada o altitud > 120 m: error.
