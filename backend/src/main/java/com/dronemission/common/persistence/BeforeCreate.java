package com.dronemission.common.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un método de una entidad que debe ejecutarse justo antes de guardar
 * un documento NUEVO en MongoDB (por ejemplo, para llenar la fecha de creación).
 * Reemplaza a @PrePersist de JPA. Lo ejecuta {@link MongoLifecycleListener}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeforeCreate {
}
