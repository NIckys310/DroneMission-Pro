-- =====================================================================
-- DroneMission Pro - Datos iniciales de demostración
-- Las fechas son relativas a CURRENT_DATE para que la demo siempre
-- tenga misiones "de hoy", "de mañana" y "de la semana pasada".
-- Contraseña de TODOS los usuarios demo: Demo2026!  (hash BCrypt)
-- =====================================================================

-- ---------------------------------------------------------------------
-- Roles
-- ---------------------------------------------------------------------
INSERT INTO roles (id, nombre, descripcion) VALUES
 (1, 'ADMINISTRADOR', 'Acceso completo al sistema'),
 (2, 'OPERADOR',      'Planifica misiones y asigna drones y pilotos'),
 (3, 'PILOTO',        'Ejecuta las misiones asignadas'),
 (4, 'TECNICO',       'Gestiona el mantenimiento de la flota'),
 (5, 'CLIENTE',       'Consulta sus misiones, evidencias y reportes');

-- ---------------------------------------------------------------------
-- Permisos
-- ---------------------------------------------------------------------
INSERT INTO permisos (id, codigo, descripcion) VALUES
 (1,  'USUARIOS_GESTIONAR',      'Crear, editar y desactivar usuarios'),
 (2,  'ROLES_GESTIONAR',         'Modificar los permisos de los roles'),
 (3,  'DRONES_VER',              'Consultar la flota de drones'),
 (4,  'DRONES_GESTIONAR',        'Crear, editar y desactivar drones'),
 (5,  'DRONES_ESTADO_TECNICO',   'Cambiar el estado técnico de un drone'),
 (6,  'PILOTOS_VER',             'Consultar pilotos'),
 (7,  'PILOTOS_GESTIONAR',       'Crear, editar y desactivar pilotos'),
 (8,  'CLIENTES_VER',            'Consultar clientes'),
 (9,  'CLIENTES_GESTIONAR',      'Crear, editar y desactivar clientes'),
 (10, 'MISIONES_VER',            'Consultar misiones (según alcance del rol)'),
 (11, 'MISIONES_GESTIONAR',      'Crear, editar, programar, asignar y cancelar misiones'),
 (12, 'MISIONES_EJECUTAR',       'Preparar, iniciar, pausar, reanudar y finalizar misiones'),
 (13, 'INCIDENTES_VER',          'Consultar todos los incidentes'),
 (14, 'INCIDENTES_REGISTRAR',    'Registrar incidentes en una misión'),
 (15, 'INCIDENTES_GESTIONAR',    'Cambiar el estado de los incidentes'),
 (16, 'EVIDENCIAS_REGISTRAR',    'Registrar evidencias en una misión'),
 (17, 'MANTENIMIENTO_VER',       'Consultar mantenimientos'),
 (18, 'MANTENIMIENTO_GESTIONAR', 'Registrar y gestionar mantenimientos'),
 (19, 'REPORTES_VER',            'Consultar y descargar reportes'),
 (20, 'REPORTES_GENERAR',        'Generar reportes'),
 (21, 'DASHBOARD_VER',           'Ver el dashboard de operaciones'),
 (22, 'AUDITORIA_VER',           'Consultar la bitácora de auditoría');

-- Administrador: todos los permisos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT 1, id FROM permisos;

-- Operador
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES
 (2, 3), (2, 6), (2, 8), (2, 10), (2, 11), (2, 13), (2, 14), (2, 15),
 (2, 16), (2, 17), (2, 19), (2, 20), (2, 21);

-- Piloto
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES
 (3, 10), (3, 12), (3, 14), (3, 16);

-- Técnico
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES
 (4, 3), (4, 5), (4, 17), (4, 18), (4, 21);

-- Cliente
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES
 (5, 10), (5, 19);

-- ---------------------------------------------------------------------
-- Usuarios demo (contraseña: Demo2026!)
-- ---------------------------------------------------------------------
INSERT INTO usuarios (id, nombre, apellidos, correo, contrasena_hash, rol_id, activo) VALUES
 (1, 'Laura',     'Gómez Salazar',   'admin@dronemission.local',    '$2a$10$P/jlsuL8yNAuzGh4ODDJt.LozX5.jtgqY5CAWeAtA2n8hPu823pYS', 1, TRUE),
 (2, 'Carlos',    'Ramírez Peña',    'operador@dronemission.local', '$2a$10$P/jlsuL8yNAuzGh4ODDJt.LozX5.jtgqY5CAWeAtA2n8hPu823pYS', 2, TRUE),
 (3, 'Andrés',    'Torres Molina',   'piloto@dronemission.local',   '$2a$10$P/jlsuL8yNAuzGh4ODDJt.LozX5.jtgqY5CAWeAtA2n8hPu823pYS', 3, TRUE),
 (4, 'Mariana',   'López Cárdenas',  'tecnico@dronemission.local',  '$2a$10$P/jlsuL8yNAuzGh4ODDJt.LozX5.jtgqY5CAWeAtA2n8hPu823pYS', 4, TRUE),
 (5, 'Sofía',     'Herrera Vélez',   'cliente@dronemission.local',  '$2a$10$P/jlsuL8yNAuzGh4ODDJt.LozX5.jtgqY5CAWeAtA2n8hPu823pYS', 5, TRUE),
 (6, 'Valentina', 'Ruiz Arango',     'piloto2@dronemission.local',  '$2a$10$P/jlsuL8yNAuzGh4ODDJt.LozX5.jtgqY5CAWeAtA2n8hPu823pYS', 3, TRUE);

-- ---------------------------------------------------------------------
-- Clientes
-- ---------------------------------------------------------------------
INSERT INTO clientes (id, tipo, nombre, documento, correo, telefono, direccion, ciudad, activo, usuario_id) VALUES
 (1, 'EMPRESA', 'AgroValle S.A.S.',                 '900123456-7', 'contacto@agrovalle.example',   '602 555 0101', 'Km 5 vía Palmira',        'Palmira',  TRUE, 5),
 (2, 'EMPRESA', 'Energía del Pacífico E.S.P.',      '890456789-1', 'operaciones@epacifico.example', '602 555 0202', 'Calle 15 # 26-40',        'Yumbo',    TRUE, NULL),
 (3, 'EMPRESA', 'Constructora Andina Ltda.',        '901987654-3', 'proyectos@candina.example',     '602 555 0303', 'Av. 6N # 23-50',          'Cali',     TRUE, NULL),
 (4, 'PERSONA', 'Jorge Medina Rojas',               '16789456',    'jorge.medina@correo.example',   '315 555 0404', 'Hacienda El Paraíso',     'El Cerrito', TRUE, NULL);

-- ---------------------------------------------------------------------
-- Pilotos y certificaciones
-- ---------------------------------------------------------------------
INSERT INTO pilotos (id, nombre, apellidos, documento, correo, telefono, numero_licencia, vencimiento_licencia, horas_vuelo, estado, usuario_id) VALUES
 (1, 'Andrés',    'Torres Molina',  '1144123456', 'piloto@dronemission.local',  '310 555 1001', 'UAEAC-RPA-2023-0145', CURRENT_DATE + INTERVAL '400' DAY, 152.50, 'DISPONIBLE',       3),
 (2, 'Valentina', 'Ruiz Arango',    '1130987654', 'piloto2@dronemission.local', '311 555 1002', 'UAEAC-RPA-2024-0288', CURRENT_DATE + INTERVAL '200' DAY,  98.00, 'DISPONIBLE',       6),
 (3, 'Diego',     'Castaño Ríos',   '94456123',   'diego.castano@dronemission.local', '312 555 1003', 'UAEAC-RPA-2021-0032', CURRENT_DATE - INTERVAL '15' DAY, 210.75, 'LICENCIA_VENCIDA', NULL),
 (4, 'Camila',    'Ortiz Becerra',  '1107456789', 'camila.ortiz@dronemission.local',  '313 555 1004', 'UAEAC-RPA-2025-0410', CURRENT_DATE + INTERVAL '300' DAY,  45.25, 'NO_DISPONIBLE',    NULL);

INSERT INTO certificaciones_piloto (piloto_id, nombre, entidad_emisora, fecha_emision, fecha_vencimiento) VALUES
 (1, 'Operación RPAS categoría específica', 'Aerocivil',            CURRENT_DATE - INTERVAL '700' DAY, CURRENT_DATE + INTERVAL '400' DAY),
 (1, 'Inspección de líneas eléctricas',      'SENA',                 CURRENT_DATE - INTERVAL '500' DAY, NULL),
 (2, 'Aplicación aérea de agroquímicos',      'ICA',                  CURRENT_DATE - INTERVAL '300' DAY, CURRENT_DATE + INTERVAL '430' DAY),
 (2, 'Operación RPAS categoría específica', 'Aerocivil',            CURRENT_DATE - INTERVAL '400' DAY, CURRENT_DATE + INTERVAL '200' DAY),
 (3, 'Fotogrametría y topografía con drones', 'Universidad del Valle', CURRENT_DATE - INTERVAL '900' DAY, NULL),
 (4, 'Vuelo nocturno con cámara térmica',    'Aerocivil',            CURRENT_DATE - INTERVAL '120' DAY, CURRENT_DATE + INTERVAL '600' DAY);

-- ---------------------------------------------------------------------
-- Drones
-- ---------------------------------------------------------------------
INSERT INTO drones (id, codigo, nombre, fabricante, modelo, numero_serie, tipo, estado, capacidad_bateria_mah, bateria_actual, horas_vuelo, velocidad_maxima_kmh, autonomia_minutos, fecha_adquisicion, ultimo_mantenimiento, proximo_mantenimiento, descripcion) VALUES
 (1, 'DRN-001', 'Halcón I',       'DJI',     'Matrice 300 RTK', 'M300-8841-A', 'INSPECCION',  'DISPONIBLE',        5935, 95, 120.50, 82, 55, CURRENT_DATE - INTERVAL '800' DAY, CURRENT_DATE - INTERVAL '30' DAY, CURRENT_DATE + INTERVAL '60' DAY, 'Drone de inspección con cámara zoom y térmica.'),
 (2, 'DRN-002', 'Agro Titán',     'DJI',     'Agras T40',       'T40-2210-B',  'AGRICULTURA', 'DISPONIBLE',       30000, 88,  76.25, 36, 18, CURRENT_DATE - INTERVAL '500' DAY, CURRENT_DATE - INTERVAL '60' DAY, CURRENT_DATE + INTERVAL '30' DAY, 'Tanque de 40 L para fumigación de precisión.'),
 (3, 'DRN-003', 'Centinela',      'Autel',   'EVO Max 4T',      'EVO4T-5521',  'VIGILANCIA',  'DISPONIBLE',        8070, 100, 64.00, 72, 42, CURRENT_DATE - INTERVAL '300' DAY, CURRENT_DATE - INTERVAL '20' DAY, CURRENT_DATE + INTERVAL '70' DAY, 'Vigilancia diurna y nocturna con sensor térmico.'),
 (4, 'DRN-004', 'Cartógrafo',     'DJI',     'Phantom 4 RTK',   'P4R-7781-C',  'TOPOGRAFIA',  'EN_MANTENIMIENTO',  5870, 60, 188.75, 58, 30, CURRENT_DATE - INTERVAL '1200' DAY, CURRENT_DATE - INTERVAL '95' DAY, CURRENT_DATE - INTERVAL '5' DAY, 'Levantamientos fotogramétricos con RTK.'),
 (5, 'DRN-005', 'Lente Aéreo',    'DJI',     'Mavic 3 Pro',     'M3P-4410-D',  'FOTOGRAFIA',  'DISPONIBLE',        5000, 76,  33.50, 75, 43, CURRENT_DATE - INTERVAL '200' DAY, CURRENT_DATE - INTERVAL '40' DAY, CURRENT_DATE + INTERVAL '50' DAY, 'Fotografía y video 5.1K para eventos y bienes raíces.'),
 (6, 'DRN-006', 'Vigía Nocturno', 'Parrot',  'Anafi USA',       'ANF-9932-E',  'VIGILANCIA',  'FUERA_DE_SERVICIO', 3400, 20, 240.00, 55, 32, CURRENT_DATE - INTERVAL '1500' DAY, CURRENT_DATE - INTERVAL '200' DAY, CURRENT_DATE + INTERVAL '2' DAY, 'Motor trasero dañado. Pendiente de reparación.'),
 (7, 'DRN-007', 'Halcón II',      'Skydio',  'X10',             'SKX10-1188',  'INSPECCION',  'DISPONIBLE',        4500, 45,  12.00, 64, 40, CURRENT_DATE - INTERVAL '90' DAY,  NULL, CURRENT_DATE + INTERVAL '10' DAY, 'Batería baja: útil para demostrar la validación de batería.'),
 (8, 'DRN-008', 'Mapper Pro',     'senseFly','eBee X',          'EBX-6620-F',  'TOPOGRAFIA',  'DISPONIBLE',        4900, 92,  58.25, 90, 90, CURRENT_DATE - INTERVAL '400' DAY, CURRENT_DATE - INTERVAL '15' DAY, CURRENT_DATE + INTERVAL '75' DAY, 'Ala fija para mapeo de grandes extensiones.');

-- ---------------------------------------------------------------------
-- Mantenimientos
-- ---------------------------------------------------------------------
INSERT INTO mantenimientos (id, drone_id, tecnico_id, tipo, fecha, descripcion, costo, estado, proxima_fecha, observaciones, iniciado_en, completado_en) VALUES
 (1, 4, 4, 'CALIBRACION', CURRENT_DATE - INTERVAL '1' DAY,  'Calibración de IMU, brújula y módulo RTK.',            350000, 'EN_PROCESO', CURRENT_DATE + INTERVAL '90' DAY, 'Desviación de 2 m detectada en el último vuelo.', CURRENT_TIMESTAMP - INTERVAL '20' HOUR, NULL),
 (2, 1, 4, 'PREVENTIVO',  CURRENT_DATE - INTERVAL '30' DAY, 'Revisión general de hélices, motores y firmware.',     280000, 'COMPLETADO', CURRENT_DATE + INTERVAL '60' DAY, 'Se actualizó el firmware a la última versión.', CURRENT_TIMESTAMP - INTERVAL '30' DAY, CURRENT_TIMESTAMP - INTERVAL '29' DAY),
 (3, 6, 4, 'REPARACION',  CURRENT_DATE + INTERVAL '2' DAY,  'Cambio del motor trasero izquierdo.',                 1200000, 'PROGRAMADO', CURRENT_DATE + INTERVAL '120' DAY, 'Repuesto en camino.', NULL, NULL),
 (4, 2, 4, 'INSPECCION',  CURRENT_DATE - INTERVAL '60' DAY, 'Inspección del sistema de aspersión y boquillas.',     150000, 'COMPLETADO', CURRENT_DATE + INTERVAL '30' DAY, NULL, CURRENT_TIMESTAMP - INTERVAL '60' DAY, CURRENT_TIMESTAMP - INTERVAL '60' DAY),
 (5, 7, 4, 'PREVENTIVO',  CURRENT_DATE + INTERVAL '10' DAY, 'Primer mantenimiento preventivo (90 días de uso).',     220000, 'PROGRAMADO', NULL, NULL, NULL, NULL);

-- ---------------------------------------------------------------------
-- Misiones
-- ---------------------------------------------------------------------
INSERT INTO misiones (id, codigo, nombre, descripcion, tipo, cliente_id, ubicacion, latitud, longitud, altitud_objetivo_m, fecha_programada, hora_inicio, hora_fin_estimada, prioridad, estado, objetivo, observaciones, creado_por, inicio_real, fin_real) VALUES
 (1, 'MSN-0001', 'Inspección torre de transmisión T-45', 'Revisión de aisladores y herrajes de la torre T-45.', 'INSPECCION', 2, 'Línea Yumbo - Cali, torre T-45', 3.585200, -76.495100, 60, CURRENT_DATE - INTERVAL '7' DAY, '08:00', '09:30', 'ALTA', 'FINALIZADA', 'Detectar puntos calientes y daños en aisladores.', 'Vuelo sin novedades mayores.', 2, (CURRENT_DATE - INTERVAL '7' DAY + INTERVAL '485' MINUTE), (CURRENT_DATE - INTERVAL '7' DAY + INTERVAL '485' MINUTE) + INTERVAL '25' MINUTE),
 (2, 'MSN-0002', 'Fumigación lote 12 de caña',           'Aplicación de fertilizante foliar en el lote 12.',   'AGRICULTURA', 1, 'Hacienda La Esperanza, lote 12', 3.539400, -76.303200, 15, CURRENT_DATE - INTERVAL '3' DAY, '06:30', '08:00', 'MEDIA', 'FINALIZADA', 'Cubrir 8 hectáreas con aplicación uniforme.', NULL, 2, (CURRENT_DATE - INTERVAL '3' DAY + INTERVAL '395' MINUTE), (CURRENT_DATE - INTERVAL '3' DAY + INTERVAL '395' MINUTE) + INTERVAL '18' MINUTE),
 (3, 'MSN-0003', 'Levantamiento topográfico Los Pinos',  'Ortofoto y modelo digital del terreno para la urbanización Los Pinos.', 'TOPOGRAFIA', 3, 'Urbanización Los Pinos, Jamundí', 3.262100, -76.539800, 80, CURRENT_DATE, '09:00', '11:00', 'ALTA', 'PROGRAMADA', 'Generar ortofoto con precisión de 3 cm/píxel.', NULL, 2, NULL, NULL),
 (4, 'MSN-0004', 'Vigilancia perimetral planta Acopi',   'Recorrido perimetral de seguridad de la subestación.', 'VIGILANCIA', 2, 'Subestación Acopi, Yumbo', 3.551300, -76.487600, 50, CURRENT_DATE + INTERVAL '2' DAY, '19:00', '20:30', 'MEDIA', 'PROGRAMADA', 'Detectar intrusiones en el perímetro.', NULL, 2, NULL, NULL),
 (5, 'MSN-0005', 'Monitoreo cultivo de café',            'Seguimiento del estado vegetativo del cultivo.',     'MONITOREO', 1, 'Finca El Mirador, El Cerrito', 3.685400, -76.312500, 40, CURRENT_DATE + INTERVAL '4' DAY, '07:00', '08:30', 'BAJA', 'PENDIENTE', 'Identificar zonas con estrés hídrico.', NULL, 2, NULL, NULL),
 (6, 'MSN-0006', 'Sesión fotográfica Hacienda El Paraíso','Fotografías aéreas para material turístico.',       'FOTOGRAFIA', 4, 'Hacienda El Paraíso, El Cerrito', 3.632900, -76.258400, 70, CURRENT_DATE + INTERVAL '6' DAY, '16:00', '17:00', 'BAJA', 'BORRADOR', 'Capturar 40 fotografías en hora dorada.', NULL, 2, NULL, NULL),
 (7, 'MSN-0007', 'Inspección puente Juanchito',          'Inspección visual de la estructura del puente.',     'INSPECCION', 3, 'Puente Juanchito, Cali', 3.452600, -76.475300, 45, CURRENT_DATE - INTERVAL '2' DAY, '10:00', '11:00', 'ALTA', 'CANCELADA', 'Detectar fisuras en las vigas principales.', 'Cancelada por falla técnica del drone asignado.', 2, NULL, NULL),
 (8, 'MSN-0008', 'Fumigación lote 7 de caña',            'Control de plagas en el lote 7.',                    'AGRICULTURA', 1, 'Hacienda La Esperanza, lote 7', 3.408700, -76.350900, 15, CURRENT_DATE + INTERVAL '5' DAY, '06:00', '07:30', 'ALTA', 'PENDIENTE', 'Aplicar control biológico en 6 hectáreas.', NULL, 2, NULL, NULL);

-- Asignaciones (drone + piloto) de cada misión
INSERT INTO asignaciones_mision (mision_id, drone_id, piloto_id, asignado_por, fecha_asignacion, activa) VALUES
 (1, 1, 1, 2, CURRENT_TIMESTAMP - INTERVAL '9' DAY, TRUE),
 (2, 2, 2, 2, CURRENT_TIMESTAMP - INTERVAL '5' DAY, TRUE),
 (3, 8, 1, 2, CURRENT_TIMESTAMP - INTERVAL '1' DAY, TRUE),
 (4, 3, 2, 2, CURRENT_TIMESTAMP - INTERVAL '1' DAY, TRUE),
 (7, 5, 1, 2, CURRENT_TIMESTAMP - INTERVAL '4' DAY, TRUE);

-- Telemetría resumida de las misiones finalizadas (datos simulados)
INSERT INTO telemetria (mision_id, registrado_en, latitud, longitud, altitud_m, velocidad_kmh, bateria, distancia_m, temperatura_c, tiempo_vuelo_s, punto_ruta_actual, simulado) VALUES
 (1, (CURRENT_DATE - INTERVAL '7' DAY + INTERVAL '485' MINUTE) + INTERVAL '1' MINUTE,  3.585200, -76.495100, 20.00,  8.00, 94,    0.00, 24.10,   60, 1, TRUE),
 (1, (CURRENT_DATE - INTERVAL '7' DAY + INTERVAL '485' MINUTE) + INTERVAL '5' MINUTE,  3.585900, -76.494600, 60.00, 22.50, 86,  310.40, 24.60,  300, 3, TRUE),
 (1, (CURRENT_DATE - INTERVAL '7' DAY + INTERVAL '485' MINUTE) + INTERVAL '10' MINUTE, 3.585600, -76.493800, 60.00, 18.20, 76,  690.80, 25.20,  600, 5, TRUE),
 (1, (CURRENT_DATE - INTERVAL '7' DAY + INTERVAL '485' MINUTE) + INTERVAL '15' MINUTE, 3.584600, -76.494300, 60.00, 21.00, 66, 1080.10, 25.80,  900, 7, TRUE),
 (1, (CURRENT_DATE - INTERVAL '7' DAY + INTERVAL '485' MINUTE) + INTERVAL '20' MINUTE, 3.584800, -76.495600, 55.00, 19.60, 57, 1460.30, 26.10, 1200, 9, TRUE),
 (1, (CURRENT_DATE - INTERVAL '7' DAY + INTERVAL '485' MINUTE) + INTERVAL '25' MINUTE, 3.585200, -76.495100,  0.00,  0.00, 49, 1620.00, 25.40, 1500, 10, TRUE),
 (2, (CURRENT_DATE - INTERVAL '3' DAY + INTERVAL '395' MINUTE) + INTERVAL '1' MINUTE,  3.539400, -76.303200,  8.00,  6.00, 87,    0.00, 21.30,   60, 1, TRUE),
 (2, (CURRENT_DATE - INTERVAL '3' DAY + INTERVAL '395' MINUTE) + INTERVAL '6' MINUTE,  3.539900, -76.302400, 15.00, 18.00, 64,  520.00, 22.00,  360, 4, TRUE),
 (2, (CURRENT_DATE - INTERVAL '3' DAY + INTERVAL '395' MINUTE) + INTERVAL '12' MINUTE, 3.540500, -76.301700, 15.00, 18.00, 38, 1240.00, 22.90,  720, 8, TRUE),
 (2, (CURRENT_DATE - INTERVAL '3' DAY + INTERVAL '395' MINUTE) + INTERVAL '18' MINUTE, 3.539400, -76.303200,  0.00,  0.00, 19, 1810.00, 23.10, 1080, 12, TRUE);

-- ---------------------------------------------------------------------
-- Evidencias
-- ---------------------------------------------------------------------
INSERT INTO evidencias (mision_id, tipo, nombre, url, descripcion, visible_cliente, subido_por, fecha) VALUES
 (1, 'FOTOGRAFIA',  'Aislador fase B',             '/demo/evidencias/torre-t45.svg',  'Punto caliente leve en el aislador de la fase B.', TRUE,  3, (CURRENT_DATE - INTERVAL '7' DAY + INTERVAL '485' MINUTE) + INTERVAL '12' MINUTE),
 (1, 'DOCUMENTO',   'Informe termográfico T-45',   '/demo/evidencias/informe-t45.svg', 'Resumen de temperaturas registradas.',             TRUE,  3, (CURRENT_DATE - INTERVAL '7' DAY + INTERVAL '485' MINUTE) + INTERVAL '40' MINUTE),
 (2, 'FOTOGRAFIA',  'Cobertura lote 12',           '/demo/evidencias/lote-12.svg',    'Vista general del lote tras la aplicación.',       TRUE,  6, (CURRENT_DATE - INTERVAL '3' DAY + INTERVAL '395' MINUTE) + INTERVAL '15' MINUTE),
 (2, 'OBSERVACION', 'Viento lateral',              '-',                               'Ráfagas de 18 km/h en el costado norte; se redujo la velocidad.', FALSE, 6, (CURRENT_DATE - INTERVAL '3' DAY + INTERVAL '395' MINUTE) + INTERVAL '7' MINUTE);

-- ---------------------------------------------------------------------
-- Incidentes
-- ---------------------------------------------------------------------
INSERT INTO incidentes (mision_id, fecha, tipo, severidad, descripcion, registrado_por, estado, resolucion) VALUES
 (1, (CURRENT_DATE - INTERVAL '7' DAY + INTERVAL '485' MINUTE) + INTERVAL '9' MINUTE,  'OBSTACULO',     'MEDIA', 'Ave rapaz cerca de la torre; se mantuvo distancia de seguridad.', 3,    'RESUELTO', 'Se esperó 2 minutos y se continuó la inspección.'),
 (2, (CURRENT_DATE - INTERVAL '3' DAY + INTERVAL '395' MINUTE) + INTERVAL '7' MINUTE,  'CLIMA',         'BAJA',  'Ráfagas de viento lateral de 18 km/h.',                           6,    'RESUELTO', 'Se redujo la velocidad de aplicación.'),
 (2, (CURRENT_DATE - INTERVAL '3' DAY + INTERVAL '395' MINUTE) + INTERVAL '17' MINUTE, 'BATERIA_BAJA',  'ALTA',  'Batería del drone por debajo del 20 % (alerta automática del sistema).', NULL, 'RESUELTO', 'El piloto finalizó la misión y aterrizó en la base.'),
 (7, CURRENT_TIMESTAMP - INTERVAL '2' DAY,                        'ERROR_TECNICO', 'ALTA',  'Falla en el gimbal de la cámara durante la prueba previa al vuelo.', 3,  'ABIERTO',  NULL);

-- ---------------------------------------------------------------------
-- Reportes históricos
-- ---------------------------------------------------------------------
INSERT INTO reportes (tipo, titulo, mision_id, generado_por, generado_en, resumen) VALUES
 ('MISION', 'Reporte de misión MSN-0001', 1, 2, (CURRENT_DATE - INTERVAL '7' DAY + INTERVAL '485' MINUTE) + INTERVAL '30' MINUTE, 'Misión finalizada en 25 min. Distancia 1620 m. Batería consumida 45 %. 1 incidente(s), 2 evidencia(s).'),
 ('MISION', 'Reporte de misión MSN-0002', 2, 2, (CURRENT_DATE - INTERVAL '3' DAY + INTERVAL '395' MINUTE) + INTERVAL '20' MINUTE, 'Misión finalizada en 18 min. Distancia 1810 m. Batería consumida 68 %. 2 incidente(s), 2 evidencia(s).');

-- ---------------------------------------------------------------------
-- Auditoría y notificaciones de ejemplo
-- ---------------------------------------------------------------------
INSERT INTO auditoria (usuario_id, correo_usuario, accion, entidad, entidad_id, descripcion, fecha) VALUES
 (2, 'operador@dronemission.local', 'CREAR',     'MISION', 3, 'Misión MSN-0003 creada', CURRENT_TIMESTAMP - INTERVAL '2' DAY),
 (2, 'operador@dronemission.local', 'ASIGNAR',   'MISION', 3, 'Drone DRN-008 y piloto Andrés Torres Molina asignados a MSN-0003', CURRENT_TIMESTAMP - INTERVAL '1' DAY),
 (2, 'operador@dronemission.local', 'CANCELAR',  'MISION', 7, 'Misión MSN-0007 cancelada: falla técnica del drone', CURRENT_TIMESTAMP - INTERVAL '2' DAY),
 (4, 'tecnico@dronemission.local',  'INICIAR',   'MANTENIMIENTO', 1, 'Mantenimiento de DRN-004 iniciado; drone EN_MANTENIMIENTO', CURRENT_TIMESTAMP - INTERVAL '20' HOUR);

INSERT INTO notificaciones (usuario_id, titulo, mensaje, mision_id, leida, fecha) VALUES
 (3, 'Nueva misión asignada', 'Se te asignó la misión MSN-0003 (Levantamiento topográfico Los Pinos) para hoy.', 3, FALSE, CURRENT_TIMESTAMP - INTERVAL '1' DAY),
 (6, 'Nueva misión asignada', 'Se te asignó la misión MSN-0004 (Vigilancia perimetral planta Acopi).', 4, FALSE, CURRENT_TIMESTAMP - INTERVAL '1' DAY),
 (5, 'Misión finalizada',     'La misión MSN-0002 finalizó. Ya puedes consultar el reporte.', 2, FALSE, (CURRENT_DATE - INTERVAL '3' DAY + INTERVAL '395' MINUTE));

-- ---------------------------------------------------------------------
-- Reiniciar las secuencias de identidad después de insertar IDs fijos
-- ---------------------------------------------------------------------
ALTER TABLE roles ALTER COLUMN id RESTART WITH 6;
ALTER TABLE permisos ALTER COLUMN id RESTART WITH 23;
ALTER TABLE usuarios ALTER COLUMN id RESTART WITH 7;
ALTER TABLE clientes ALTER COLUMN id RESTART WITH 5;
ALTER TABLE pilotos ALTER COLUMN id RESTART WITH 5;
ALTER TABLE drones ALTER COLUMN id RESTART WITH 9;
ALTER TABLE mantenimientos ALTER COLUMN id RESTART WITH 6;
ALTER TABLE misiones ALTER COLUMN id RESTART WITH 9;
