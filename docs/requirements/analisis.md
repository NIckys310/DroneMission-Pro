# Fase 1 — Análisis

## Descripción

**DroneMission Pro** es una plataforma web para administrar de forma centralizada las operaciones de una empresa que presta servicios con drones: inspección de infraestructura, agricultura de precisión, vigilancia, topografía, fotografía y monitoreo. Permite registrar drones, pilotos y clientes; planificar misiones; asignar recursos; seguir el vuelo con telemetría; registrar incidentes y evidencias; gestionar mantenimientos y generar reportes.

> **Drone virtual.** El proyecto no usa un drone físico. Cada misión tiene un **plan de vuelo programado con anticipación** (puntos de ruta generados por la estrategia de su tipo). Cuando el piloto despega, un **simulador** recorre ese plan y genera telemetría claramente marcada como *datos simulados de demostración*. El diseño permite reemplazar el simulador por un drone real sin cambiar el resto del sistema.

## Problema

Las empresas pequeñas y medianas de servicios con drones suelen controlar sus operaciones con hojas de cálculo y mensajes: no saben con certeza qué drone está disponible, se asignan pilotos con licencia vencida, se cruzan horarios, se olvidan mantenimientos y los clientes no tienen un lugar para consultar el resultado de sus misiones. No existe trazabilidad de quién hizo qué.

## Objetivo general

Diseñar y desarrollar un sistema de software que permita administrar de forma organizada, segura y eficiente las misiones realizadas mediante drones, centralizando la gestión de usuarios, drones, pilotos, clientes, misiones, asignaciones, evidencias, mantenimientos, incidentes y reportes.

## Objetivos específicos

1. Registrar y administrar drones, pilotos (con certificaciones), clientes y usuarios.
2. Controlar el acceso con autenticación JWT y autorización por roles y permisos.
3. Crear, programar y asignar misiones validando las reglas de negocio de disponibilidad.
4. Controlar el ciclo de vida de la misión con una máquina de estados.
5. Simular la telemetría de un drone virtual que sigue un plan de vuelo programado.
6. Registrar incidentes, evidencias y mantenimientos, con cambios automáticos de estado.
7. Generar reportes en PDF y un dashboard con indicadores y alertas.
8. Mantener trazabilidad mediante auditoría y notificaciones.

## Alcance

**Incluye:** frontend web responsive, API REST, base de datos relacional con migraciones, seguridad, 5 roles, CRUD completos, misiones con plan de vuelo, simulador de telemetría, incidentes, evidencias con subida de archivos, mantenimiento, reportes PDF, dashboard, auditoría, notificaciones internas, pruebas, diagramas y documentación.

**No incluye (trabajo futuro):** conexión a drones reales (MAVLink/DJI SDK), mapas satelitales en línea, envío real de correos, aplicación móvil, facturación.

## Actores

| Actor | Responsabilidad |
|---|---|
| **Administrador** | Acceso completo: usuarios, roles, catálogos, reportes y auditoría. |
| **Operador** | Planifica misiones, asigna drones y pilotos, cancela y gestiona incidentes. |
| **Piloto** | Ejecuta sus misiones: chequeo prevuelo, despegue, pausa, finalización, incidentes y evidencias. |
| **Técnico** | Mantenimiento de la flota y estado técnico de los drones. |
| **Cliente** | Consulta sus misiones, evidencias y reportes. |
| **Drone virtual** (sistema) | Recibe el plan de vuelo y produce telemetría simulada. |

## Documentos relacionados

- [Requerimientos funcionales](requerimientos-funcionales.md)
- [Requerimientos no funcionales](requerimientos-no-funcionales.md)
- [Reglas de negocio](reglas-de-negocio.md)
- [Casos de uso](../use-cases/README.md)
