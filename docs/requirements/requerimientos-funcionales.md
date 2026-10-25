# Requerimientos funcionales
Cada requerimiento está implementado y trazado en la [matriz de trazabilidad](../trazabilidad.md).

## RF-001 — Iniciar sesión
| Campo | Detalle |
|---|---|
| **Código** | RF-001 |
| **Nombre** | Iniciar sesión |
| **Descripción** | El sistema autentica al usuario con correo y contraseña y entrega un token JWT con su rol y permisos. |
| **Actor** | Todos |
| **Precondición** | El usuario está registrado y activo. |
| **Flujo principal** | 1. Ingresa correo y contraseña.<br>2. El sistema valida el formato.<br>3. El sistema verifica la contraseña cifrada (BCrypt).<br>4. Genera el token JWT (8 h).<br>5. Redirige al inicio según el rol. |
| **Flujo alternativo** | • Contraseña incorrecta o usuario inexistente: 401 'Correo o contraseña incorrectos'.<br>• Usuario inactivo: 401 'El usuario está inactivo'. |
| **Postcondición** | Sesión iniciada; queda auditoría LOGIN. |
| **Caso de uso** | CU-001 |

## RF-002 — Cerrar sesión
| Campo | Detalle |
|---|---|
| **Código** | RF-002 |
| **Nombre** | Cerrar sesión |
| **Descripción** | El usuario cierra la sesión; el frontend descarta el token y se audita. |
| **Actor** | Todos |
| **Precondición** | Sesión iniciada. |
| **Flujo principal** | 1. Pulsa 'Cerrar sesión'.<br>2. El sistema registra LOGOUT.<br>3. Vuelve al login. |
| **Flujo alternativo** | • Token vencido: se redirige al login. |
| **Postcondición** | Sesión cerrada. |
| **Caso de uso** | CU-001 |

## RF-003 — Recuperar contraseña (simulado)
| Campo | Detalle |
|---|---|
| **Código** | RF-003 |
| **Nombre** | Recuperar contraseña (simulado) |
| **Descripción** | Genera un token temporal de 30 minutos para restablecer la contraseña (se muestra en pantalla en lugar de enviarse por correo). |
| **Actor** | Todos |
| **Precondición** | El correo existe. |
| **Flujo principal** | 1. Ingresa su correo.<br>2. El sistema genera el token.<br>3. Ingresa token y nueva contraseña.<br>4. El sistema guarda el nuevo hash. |
| **Flujo alternativo** | • Token inválido o vencido: 400. |
| **Postcondición** | Contraseña actualizada. |
| **Caso de uso** | CU-001 |

## RF-004 — Gestionar usuarios
| Campo | Detalle |
|---|---|
| **Código** | RF-004 |
| **Nombre** | Gestionar usuarios |
| **Descripción** | Crear, editar y desactivar usuarios, asignar su rol y vincularlos con su ficha de piloto o cliente. |
| **Actor** | Administrador |
| **Precondición** | Sesión de administrador. |
| **Flujo principal** | 1. Abre Usuarios.<br>2. Crea o edita el usuario y su rol.<br>3. El sistema valida correo único y contraseña.<br>4. Guarda y audita. |
| **Flujo alternativo** | • Correo duplicado: 409.<br>• Intentar desactivarse a sí mismo: 400. |
| **Postcondición** | Usuario guardado. |
| **Caso de uso** | CU-002 |

## RF-005 — Gestionar roles y permisos
| Campo | Detalle |
|---|---|
| **Código** | RF-005 |
| **Nombre** | Gestionar roles y permisos |
| **Descripción** | Consultar la matriz rol × permiso y modificar los permisos de cada rol (excepto el administrador). |
| **Actor** | Administrador |
| **Precondición** | Sesión de administrador. |
| **Flujo principal** | 1. Abre Roles y permisos.<br>2. Marca o desmarca permisos.<br>3. Guarda. |
| **Flujo alternativo** | • Modificar el rol ADMINISTRADOR: 400. |
| **Postcondición** | Permisos aplicados en la siguiente petición. |
| **Caso de uso** | CU-002 |

## RF-006 — Registrar y editar drones
| Campo | Detalle |
|---|---|
| **Código** | RF-006 |
| **Nombre** | Registrar y editar drones |
| **Descripción** | Registrar drones con sus datos técnicos; el Factory completa los valores típicos por tipo y genera el código. |
| **Actor** | Administrador |
| **Precondición** | Sesión con DRONES_GESTIONAR. |
| **Flujo principal** | 1. Abre Drones > Nuevo.<br>2. Completa el formulario.<br>3. El sistema valida y aplica valores por defecto.<br>4. Guarda en estado DISPONIBLE. |
| **Flujo alternativo** | • Número de serie o código duplicado: 409.<br>• Batería fuera de 0-100: 400. |
| **Postcondición** | Drone disponible para misiones. |
| **Caso de uso** | CU-003 |

## RF-007 — Consultar, buscar y filtrar drones
| Campo | Detalle |
|---|---|
| **Código** | RF-007 |
| **Nombre** | Consultar, buscar y filtrar drones |
| **Descripción** | Listar la flota con búsqueda por texto y filtros por estado y tipo; ver detalle e historial. |
| **Actor** | Administrador, Operador, Técnico |
| **Precondición** | Permiso DRONES_VER. |
| **Flujo principal** | 1. Abre Drones.<br>2. Escribe o filtra.<br>3. Abre el detalle e historial. |
| **Flujo alternativo** | • Sin resultados: mensaje vacío. |
| **Postcondición** | — |
| **Caso de uso** | CU-003 |

## RF-008 — Desactivar drones
| Campo | Detalle |
|---|---|
| **Código** | RF-008 |
| **Nombre** | Desactivar drones |
| **Descripción** | Baja lógica de un drone sin misiones abiertas, conservando su historial. |
| **Actor** | Administrador |
| **Precondición** | Drone sin misiones programadas/en curso. |
| **Flujo principal** | 1. Pulsa Desactivar.<br>2. Confirma. |
| **Flujo alternativo** | • Tiene misiones abiertas: 400. |
| **Postcondición** | Drone inactivo y FUERA_DE_SERVICIO. |
| **Caso de uso** | CU-003 |

## RF-009 — Actualizar estado técnico y batería
| Campo | Detalle |
|---|---|
| **Código** | RF-009 |
| **Nombre** | Actualizar estado técnico y batería |
| **Descripción** | El técnico marca un drone DISPONIBLE o FUERA_DE_SERVICIO y carga su batería. |
| **Actor** | Técnico |
| **Precondición** | Permiso DRONES_ESTADO_TECNICO. |
| **Flujo principal** | 1. Abre el detalle del drone.<br>2. Cambia el estado o carga la batería. |
| **Flujo alternativo** | • Drone en misión: 400.<br>• Mantenimiento en proceso al marcar disponible: 400. |
| **Postcondición** | Estado actualizado y auditado. |
| **Caso de uso** | CU-014 |

## RF-010 — Gestionar pilotos
| Campo | Detalle |
|---|---|
| **Código** | RF-010 |
| **Nombre** | Gestionar pilotos |
| **Descripción** | Crear, editar y desactivar pilotos con licencia y certificaciones. |
| **Actor** | Administrador |
| **Precondición** | Permiso PILOTOS_GESTIONAR. |
| **Flujo principal** | 1. Abre Pilotos > Nuevo.<br>2. Completa datos, licencia y certificaciones.<br>3. Guarda. |
| **Flujo alternativo** | • Documento, correo o licencia duplicados: 409.<br>• Licencia vencida: queda LICENCIA_VENCIDA. |
| **Postcondición** | Piloto registrado. |
| **Caso de uso** | CU-004 |

## RF-011 — Controlar vigencia de licencias
| Campo | Detalle |
|---|---|
| **Código** | RF-011 |
| **Nombre** | Controlar vigencia de licencias |
| **Descripción** | Marcar automáticamente (al arrancar y diariamente) los pilotos con licencia vencida. |
| **Actor** | Sistema |
| **Precondición** | — |
| **Flujo principal** | 1. El planificador revisa las licencias.<br>2. Cambia el estado a LICENCIA_VENCIDA. |
| **Flujo alternativo** | • Piloto en misión: no se modifica. |
| **Postcondición** | Piloto no asignable. |
| **Caso de uso** | CU-004 |

## RF-012 — Gestionar clientes
| Campo | Detalle |
|---|---|
| **Código** | RF-012 |
| **Nombre** | Gestionar clientes |
| **Descripción** | Crear, editar, buscar y desactivar clientes; consultar sus misiones. |
| **Actor** | Administrador (Operador consulta) |
| **Precondición** | Permiso CLIENTES_GESTIONAR. |
| **Flujo principal** | 1. Abre Clientes.<br>2. Crea o edita.<br>3. Guarda. |
| **Flujo alternativo** | • Documento duplicado: 409.<br>• Cliente con misiones abiertas al desactivar: 400. |
| **Postcondición** | Cliente guardado. |
| **Caso de uso** | CU-005 |

## RF-013 — Crear misión
| Campo | Detalle |
|---|---|
| **Código** | RF-013 |
| **Nombre** | Crear misión |
| **Descripción** | Crear la misión con el Builder; queda en BORRADOR con el plan de vuelo generado por la estrategia del tipo. |
| **Actor** | Operador, Administrador |
| **Precondición** | Permiso MISIONES_GESTIONAR. |
| **Flujo principal** | 1. Abre Nueva misión.<br>2. Ingresa los datos.<br>3. El sistema valida horario, fecha y altitud.<br>4. Genera el plan de vuelo.<br>5. Guarda en BORRADOR. |
| **Flujo alternativo** | • Datos inválidos: 400 con errores por campo.<br>• Cliente inactivo: 400. |
| **Postcondición** | Misión en BORRADOR. |
| **Caso de uso** | CU-006 |

## RF-014 — Editar y eliminar misión
| Campo | Detalle |
|---|---|
| **Código** | RF-014 |
| **Nombre** | Editar y eliminar misión |
| **Descripción** | Editar la misión antes del vuelo y eliminar solo borradores. |
| **Actor** | Operador |
| **Precondición** | Misión BORRADOR, PENDIENTE o PROGRAMADA. |
| **Flujo principal** | 1. Abre la misión.<br>2. Edita o elimina. |
| **Flujo alternativo** | • Misión en vuelo o finalizada: 409.<br>• Cambiar horario de una misión programada: 400. |
| **Postcondición** | Misión actualizada; plan regenerado si cambió la ubicación. |
| **Caso de uso** | CU-006 |

## RF-015 — Programar misión
| Campo | Detalle |
|---|---|
| **Código** | RF-015 |
| **Nombre** | Programar misión |
| **Descripción** | Enviar el borrador a planificación (BORRADOR → PENDIENTE). |
| **Actor** | Operador |
| **Precondición** | Misión en BORRADOR con plan de vuelo. |
| **Flujo principal** | 1. Pulsa 'Enviar a planificación'. |
| **Flujo alternativo** | • Estado distinto: 409. |
| **Postcondición** | Misión PENDIENTE. |
| **Caso de uso** | CU-007 |

## RF-016 — Asignar drone y piloto
| Campo | Detalle |
|---|---|
| **Código** | RF-016 |
| **Nombre** | Asignar drone y piloto |
| **Descripción** | Asignar recursos validando disponibilidad, batería, compatibilidad, licencia y cruces de horario. |
| **Actor** | Operador |
| **Precondición** | Misión PENDIENTE o PROGRAMADA. |
| **Flujo principal** | 1. Abre 'Asignar recursos'.<br>2. El sistema muestra elegibles y motivos.<br>3. Selecciona drone y piloto.<br>4. El sistema valida y guarda.<br>5. PENDIENTE → PROGRAMADA y notifica. |
| **Flujo alternativo** | • Drone en mantenimiento, fuera de servicio, sin batería o incompatible: 400.<br>• Piloto con licencia vencida o no disponible: 400.<br>• Cruce de horario: 400. |
| **Postcondición** | Misión PROGRAMADA con asignación activa. |
| **Caso de uso** | CU-008 |

## RF-017 — Preparar e iniciar misión
| Campo | Detalle |
|---|---|
| **Código** | RF-017 |
| **Nombre** | Preparar e iniciar misión |
| **Descripción** | El piloto asignado completa el chequeo prevuelo (revalidación) y despega. |
| **Actor** | Piloto |
| **Precondición** | Misión PROGRAMADA asignada al piloto. |
| **Flujo principal** | 1. Completa la lista de chequeo.<br>2. El sistema revalida licencia, disponibilidad y batería.<br>3. PROGRAMADA → EN_PREPARACION.<br>4. Pulsa Despegar: EN_VUELO. |
| **Flujo alternativo** | • Otro usuario: 403.<br>• Sin drone/piloto o licencia vencida: 400.<br>• Despegar sin chequeo: 409. |
| **Postcondición** | Misión EN_VUELO; drone y piloto EN_MISION. |
| **Caso de uso** | CU-009 |

## RF-018 — Pausar y reanudar misión
| Campo | Detalle |
|---|---|
| **Código** | RF-018 |
| **Nombre** | Pausar y reanudar misión |
| **Descripción** | El piloto pausa y reanuda el vuelo. |
| **Actor** | Piloto |
| **Precondición** | Misión EN_VUELO / PAUSADA. |
| **Flujo principal** | 1. Pulsa Pausar (motivo opcional).<br>2. Pulsa Reanudar. |
| **Flujo alternativo** | • Estado inválido: 409. |
| **Postcondición** | Estado actualizado y notificado. |
| **Caso de uso** | CU-010 |

## RF-019 — Finalizar misión
| Campo | Detalle |
|---|---|
| **Código** | RF-019 |
| **Nombre** | Finalizar misión |
| **Descripción** | El piloto finaliza; se suman horas, se liberan recursos y se genera el reporte. |
| **Actor** | Piloto |
| **Precondición** | Misión EN_VUELO o PAUSADA. |
| **Flujo principal** | 1. Pulsa Finalizar con observaciones.<br>2. El sistema resume la telemetría.<br>3. Actualiza horas y batería.<br>4. Genera el reporte. |
| **Flujo alternativo** | • Misión finalizada no se reabre: 409. |
| **Postcondición** | Misión FINALIZADA, reporte en historial. |
| **Caso de uso** | CU-011 |

## RF-020 — Cancelar misión
| Campo | Detalle |
|---|---|
| **Código** | RF-020 |
| **Nombre** | Cancelar misión |
| **Descripción** | Cancelar una misión antes del vuelo indicando el motivo. |
| **Actor** | Operador |
| **Precondición** | Misión antes de EN_VUELO. |
| **Flujo principal** | 1. Pulsa Cancelar.<br>2. Escribe el motivo. |
| **Flujo alternativo** | • Misión en vuelo: 409. |
| **Postcondición** | Misión CANCELADA; recursos liberados. |
| **Caso de uso** | CU-018 |

## RF-021 — Simular telemetría
| Campo | Detalle |
|---|---|
| **Código** | RF-021 |
| **Nombre** | Simular telemetría |
| **Descripción** | El drone virtual recorre el plan de vuelo y genera lecturas marcadas como simuladas cada 2 s. |
| **Actor** | Sistema |
| **Precondición** | Misión EN_VUELO. |
| **Flujo principal** | 1. El simulador avanza al siguiente punto.<br>2. Calcula altitud, velocidad, batería, distancia, temperatura y tiempo.<br>3. Guarda la lectura. |
| **Flujo alternativo** | • Batería ≤ 20 %: incidente automático BATERIA_BAJA.<br>• Misión pausada: no avanza. |
| **Postcondición** | Telemetría disponible. |
| **Caso de uso** | CU-017 |

## RF-022 — Monitorear telemetría
| Campo | Detalle |
|---|---|
| **Código** | RF-022 |
| **Nombre** | Monitorear telemetría |
| **Descripción** | Ver el mapa, indicadores y gráficas del vuelo en vivo. |
| **Actor** | Piloto, Operador, Administrador, Cliente |
| **Precondición** | Acceso a la misión. |
| **Flujo principal** | 1. Abre 'Vuelo y telemetría'.<br>2. El frontend consulta cada 2 s. |
| **Flujo alternativo** | • Sin lecturas: mensaje informativo. |
| **Postcondición** | — |
| **Caso de uso** | CU-017 |

## RF-023 — Registrar incidente
| Campo | Detalle |
|---|---|
| **Código** | RF-023 |
| **Nombre** | Registrar incidente |
| **Descripción** | Registrar incidentes con tipo, severidad y descripción; uno CRÍTICO pausa el vuelo y notifica. |
| **Actor** | Piloto, Operador, Administrador |
| **Precondición** | Misión no borrador ni cancelada. |
| **Flujo principal** | 1. Pulsa Incidente.<br>2. Completa el formulario. |
| **Flujo alternativo** | • No es su misión: 403.<br>• Severidad crítica: pausa automática. |
| **Postcondición** | Incidente ABIERTO. |
| **Caso de uso** | CU-012 |

## RF-024 — Gestionar incidentes
| Campo | Detalle |
|---|---|
| **Código** | RF-024 |
| **Nombre** | Gestionar incidentes |
| **Descripción** | Consultar y filtrar incidentes y cambiar su estado con la resolución. |
| **Actor** | Operador, Administrador |
| **Precondición** | Permiso INCIDENTES_GESTIONAR. |
| **Flujo principal** | 1. Abre Incidentes.<br>2. Filtra.<br>3. Gestiona el estado. |
| **Flujo alternativo** | • Resolver sin resolución: 400. |
| **Postcondición** | Incidente actualizado. |
| **Caso de uso** | CU-012 |

## RF-025 — Registrar evidencia
| Campo | Detalle |
|---|---|
| **Código** | RF-025 |
| **Nombre** | Registrar evidencia |
| **Descripción** | Subir archivos o registrar URL/observaciones como evidencias de la misión. |
| **Actor** | Piloto, Operador |
| **Precondición** | Misión en preparación, vuelo, pausada o finalizada. |
| **Flujo principal** | 1. Pulsa Evidencia.<br>2. Sube archivo o escribe URL.<br>3. Guarda. |
| **Flujo alternativo** | • Tipo de archivo no permitido o > 20 MB: 400. |
| **Postcondición** | Evidencia registrada. |
| **Caso de uso** | CU-013 |

## RF-026 — Gestionar mantenimiento
| Campo | Detalle |
|---|---|
| **Código** | RF-026 |
| **Nombre** | Gestionar mantenimiento |
| **Descripción** | Registrar, iniciar, completar y cancelar mantenimientos; el drone cambia de estado automáticamente. |
| **Actor** | Técnico |
| **Precondición** | Permiso MANTENIMIENTO_GESTIONAR. |
| **Flujo principal** | 1. Registra el mantenimiento.<br>2. Lo inicia: drone EN_MANTENIMIENTO.<br>3. Lo completa: drone DISPONIBLE. |
| **Flujo alternativo** | • Drone en misión: 400.<br>• Próxima fecha no futura: 400. |
| **Postcondición** | Historial actualizado. |
| **Caso de uso** | CU-014 |

## RF-027 — Generar reportes
| Campo | Detalle |
|---|---|
| **Código** | RF-027 |
| **Nombre** | Generar reportes |
| **Descripción** | Generar y descargar en PDF los reportes de misión, drones, pilotos y mantenimiento; llevar el historial. |
| **Actor** | Administrador, Operador, Cliente (sus misiones) |
| **Precondición** | Permiso REPORTES_VER / REPORTES_GENERAR. |
| **Flujo principal** | 1. Abre Reportes.<br>2. Descarga el PDF. |
| **Flujo alternativo** | • Cliente con misión no finalizada: 403. |
| **Postcondición** | Reporte registrado en el historial. |
| **Caso de uso** | CU-015 |

## RF-028 — Consultar dashboard
| Campo | Detalle |
|---|---|
| **Código** | RF-028 |
| **Nombre** | Consultar dashboard |
| **Descripción** | Ver indicadores, gráficas, alertas, incidentes y mantenimientos pendientes. |
| **Actor** | Administrador, Operador, Técnico |
| **Precondición** | Permiso DASHBOARD_VER. |
| **Flujo principal** | 1. Abre Inicio. |
| **Flujo alternativo** | • — |
| **Postcondición** | — |
| **Caso de uso** | CU-016 |

## RF-029 — Auditoría
| Campo | Detalle |
|---|---|
| **Código** | RF-029 |
| **Nombre** | Auditoría |
| **Descripción** | Registrar y consultar las operaciones importantes (login, misiones, asignaciones, mantenimientos, bajas). |
| **Actor** | Administrador |
| **Precondición** | Permiso AUDITORIA_VER. |
| **Flujo principal** | 1. Abre Auditoría.<br>2. Filtra. |
| **Flujo alternativo** | • — |
| **Postcondición** | — |
| **Caso de uso** | CU-016 |

## RF-030 — Notificaciones
| Campo | Detalle |
|---|---|
| **Código** | RF-030 |
| **Nombre** | Notificaciones |
| **Descripción** | Avisar a los usuarios interesados cuando una misión cambia (asignación, despegue, fin, cancelación, incidente crítico). |
| **Actor** | Todos |
| **Precondición** | Sesión iniciada. |
| **Flujo principal** | 1. Ocurre un evento.<br>2. El observador crea la notificación.<br>3. El usuario la ve en la campanita. |
| **Flujo alternativo** | • — |
| **Postcondición** | Notificación leída. |
| **Caso de uso** | CU-011 |
