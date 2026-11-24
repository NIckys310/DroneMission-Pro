package com.dronemission.common.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un método de una entidad que debe ejecutarse justo antes de ACTUALIZAR
 * un documento que ya existe en MongoDB (por ejemplo, la fecha de modificación).
 * Reemplaza a @PreUpdate de JPA. Lo ejecuta {@link MongoLifecycleListener}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeforeUpdate {
}
