# CU-009 — Preparar e iniciar misión

| | |
|---|---|
| **Actor** | Piloto |
| **Precondición** | Misión PROGRAMADA asignada al piloto. |
| **Requerimientos** | RF-017 |
| **Postcondición** | Misión EN_VUELO. |

## Flujo principal

1. Abre la misión desde su inicio.
2. Completa la lista de chequeo prevuelo.
3. El sistema revalida licencia, disponibilidad y batería.
4. La misión pasa a EN_PREPARACION y el plan se carga en el drone virtual.
5. Pulsa Despegar: EN_VUELO.
6. Comienza la telemetría simulada.

## Flujos alternativos

- 1a. Otro piloto intenta operar: 403.
- 3a. Licencia vencida o batería insuficiente: rechazo.
- 5a. Despegar sin chequeo: transición inválida.
