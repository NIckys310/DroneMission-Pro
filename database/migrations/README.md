# Migraciones

Las migraciones **oficiales** viven en el backend y las ejecuta **Flyway** automáticamente al arrancar:

| Versión | Archivo | Contenido |
|---------|---------|-----------|
| V1 | `backend/src/main/resources/db/migration/V1__esquema_inicial.sql` | Tablas, llaves, restricciones e índices |
| V2 | `backend/src/main/resources/db/migration/V2__datos_iniciales.sql` | Datos de demostración |

`database/schema.sql` y `database/seed.sql` son copias idénticas para entregar o ejecutar a mano:

```bash
psql -U dronemission -d dronemission -f database/schema.sql -f database/seed.sql
```

## ¿Cómo agrego un cambio a la base de datos?

1. **Nunca** modifiques un archivo `V1`/`V2` ya ejecutado (Flyway guarda un checksum y fallaría).
2. Crea un archivo nuevo con el siguiente número, por ejemplo
   `V3__agregar_peso_drone.sql`:
   ```sql
   ALTER TABLE drones ADD COLUMN peso_kg NUMERIC(6,2);
   ```
3. Agrega el atributo a la entidad Java (`Drone.java`) con `@Column(name = "peso_kg")`.
4. Reinicia el backend: Flyway aplica la migración y Hibernate valida que coincida.

> Con la base H2 de desarrollo, si quieres empezar desde cero basta con borrar la carpeta `backend/data/`.
