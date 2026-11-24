package com.dronemission.common.persistence;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * "Unidad de trabajo" de una transacción: recuerda los documentos que se leyeron
 * (y cómo estaban) para guardar al final SOLO los que cambiaron.
 *
 * ¿Por qué existe? Con JPA/SQL, si un servicio hacía drone.setStatus(EN_MISION) dentro de un
 * método @Transactional, Hibernate guardaba el cambio solo al terminar ("dirty checking").
 * MongoDB no hace eso por sí mismo. Esta clase lo imita, y así los servicios del negocio
 * siguen igual que antes, sin agregar save() en cada lugar.
 *
 * GRASP: Fabricación Pura + Indirección (los servicios no saben cómo se guardan los cambios).
 */
public class UnitOfWork {

    /** Llave con la que Spring guarda la unidad de trabajo del hilo (petición) actual. */
    public static final Object KEY = UnitOfWork.class;

    private final boolean readOnly;
    private boolean flushing;
    // IdentityHashMap: se compara por objeto en memoria, no por equals()
    private final Map<Object, Snapshot> tracked = new IdentityHashMap<>();

    public UnitOfWork(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /** ¿Hay que vigilar documentos? No en transacciones de solo lectura ni mientras se guarda. */
    public boolean isTracking() {
        return !readOnly && !flushing;
    }

    public boolean isTracked(Object entity) {
        return tracked.containsKey(entity);
    }

    /** Guarda una "foto" del documento tal como está en la base de datos. */
    public void track(Object entity, Document original, String collection) {
        tracked.put(entity, new Snapshot(original, collection));
    }

    /**
     * Al confirmar la transacción: convierte cada objeto vigilado a documento y, si es distinto
     * de la foto original, lo guarda. Los que no cambiaron no se tocan.
     */
    public void flush(MongoTemplate template) {
        if (readOnly) {
            return;
        }
        flushing = true;
        try {
            tracked.forEach((entity, snapshot) -> {
                Document current = new Document();
                template.getConverter().write(entity, current);
                if (!current.equals(snapshot.original())) {
                    template.save(entity, snapshot.collection());
                }
            });
        } finally {
            flushing = false;
            tracked.clear();
        }
    }

    /** Foto del documento original y colección donde vive. */
    private record Snapshot(Document original, String collection) {
    }
}
