package com.dronemission.common.persistence;

import org.bson.Document;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.LazyLoadingProxy;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Escucha los eventos de MongoDB (patrón Observer de Spring Data) para:
 *  1. Antes de guardar: poner el ID numérico a los documentos nuevos y ejecutar los métodos
 *     marcados con @BeforeCreate / @BeforeUpdate (fechas de creación y modificación).
 *  2. Después de leer o guardar: registrar el documento en la {@link UnitOfWork} de la
 *     transacción actual, para guardar al final solo lo que cambió.
 */
@Component
public class MongoLifecycleListener extends AbstractMongoEventListener<Object> {

    private final SequenceGeneratorService sequenceGenerator;
    private final MongoTemplate mongoTemplate;

    // @Lazy: MongoTemplate se crea después de este listener (evita una dependencia circular)
    public MongoLifecycleListener(SequenceGeneratorService sequenceGenerator, @Lazy MongoTemplate mongoTemplate) {
        this.sequenceGenerator = sequenceGenerator;
        this.mongoTemplate = mongoTemplate;
    }

    /** 1. Justo antes de convertir el objeto Java a documento para guardarlo. */
    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object entity = event.getSource();
        MongoPersistentEntity<?> meta = metadata(entity);
        if (meta == null || !meta.hasIdProperty()) {
            return;
        }
        MongoPersistentProperty idProperty = meta.getRequiredIdProperty();
        PersistentPropertyAccessor<Object> accessor = meta.getPropertyAccessor(entity);
        boolean isNew = accessor.getProperty(idProperty) == null;
        if (isNew && Long.class.equals(idProperty.getType())) {
            // Documento nuevo: toma el siguiente número de la secuencia de su colección
            accessor.setProperty(idProperty, sequenceGenerator.next(event.getCollectionName()));
        }
        invokeAnnotated(entity, isNew ? BeforeCreate.class : BeforeUpdate.class);
    }

    /** 2a. Después de leer un documento de la base: se guarda su "foto" original. */
    @Override
    public void onAfterConvert(AfterConvertEvent<Object> event) {
        track(event.getSource(), event.getDocument(), event.getCollectionName());
    }

    /** 2b. Después de guardar: la foto pasa a ser lo que se acaba de escribir. */
    @Override
    public void onAfterSave(AfterSaveEvent<Object> event) {
        track(event.getSource(), event.getDocument(), event.getCollectionName());
    }

    private void track(Object entity, Document document, String collection) {
        UnitOfWork unitOfWork = (UnitOfWork) TransactionSynchronizationManager.getResource(UnitOfWork.KEY);
        if (unitOfWork == null || !unitOfWork.isTracking() || entity == null || document == null) {
            return; // fuera de una transacción (o de solo lectura) no hay nada que vigilar
        }
        unitOfWork.track(entity, new Document(document), collection);
        trackReferences(unitOfWork, entity);
    }

    /**
     * Los objetos referenciados con @DBRef (por ejemplo el drone de una asignación) también se
     * vigilan: si un servicio hace asignacion.getDrone().setStatus(...), ese drone se guarda.
     * Las referencias "lazy" no se recorren (no se han cargado todavía).
     */
    private void trackReferences(UnitOfWork unitOfWork, Object entity) {
        MongoPersistentEntity<?> meta = metadata(entity);
        if (meta == null) {
            return;
        }
        PersistentPropertyAccessor<Object> accessor = meta.getPropertyAccessor(entity);
        meta.doWithProperties((MongoPersistentProperty property) -> {
            if (!property.isDbReference() || property.getDBRef() == null || property.getDBRef().lazy()) {
                return;
            }
            Object value = accessor.getProperty(property);
            if (value instanceof Collection<?> many) {
                many.forEach(item -> trackReference(unitOfWork, item));
            } else {
                trackReference(unitOfWork, value);
            }
        });
    }

    private void trackReference(UnitOfWork unitOfWork, Object reference) {
        if (reference == null || reference instanceof LazyLoadingProxy || unitOfWork.isTracked(reference)) {
            return;
        }
        // La foto de la referencia se toma ahora, recién cargada, antes de que alguien la cambie
        Document snapshot = new Document();
        mongoTemplate.getConverter().write(reference, snapshot);
        unitOfWork.track(reference, snapshot, mongoTemplate.getCollectionName(reference.getClass()));
        trackReferences(unitOfWork, reference);
    }

    /** Información de mapeo (campos, id, referencias) de la clase del objeto. */
    private MongoPersistentEntity<?> metadata(Object entity) {
        if (entity instanceof LazyLoadingProxy) {
            return null;
        }
        return mongoTemplate.getConverter().getMappingContext().getPersistentEntity(entity.getClass());
    }

    /** Ejecuta los métodos de la entidad marcados con la anotación indicada. */
    private static void invokeAnnotated(Object entity, Class<? extends Annotation> annotation) {
        ReflectionUtils.doWithMethods(entity.getClass(), (Method method) -> {
            ReflectionUtils.makeAccessible(method);
            ReflectionUtils.invokeMethod(method, entity);
        }, method -> method.isAnnotationPresent(annotation));
    }
}
