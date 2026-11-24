package com.dronemission.common.persistence;

import org.bson.Document;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * Genera IDs numéricos consecutivos (1, 2, 3...) para cada colección.
 *
 * MongoDB, por defecto, usa ObjectId (un texto largo como "665f1c2e..."). Este proyecto y su
 * frontend usan IDs numéricos (/api/drones/3), así que se imita el "autoincremento" de SQL:
 * la colección "secuencias" guarda un contador por colección: { _id: "drones", valor: 8 }.
 *
 * GRASP: Fabricación Pura (no es un concepto del negocio, existe para que la persistencia funcione).
 */
@Service
public class SequenceGeneratorService {

    public static final String COLLECTION = "secuencias";

    private final MongoTemplate mongoTemplate;

    // @Lazy: MongoTemplate se termina de crear después de los listeners que usan este servicio
    public SequenceGeneratorService(@Lazy MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /** Suma 1 al contador de la colección (de forma atómica) y devuelve el nuevo valor. */
    public long next(String collectionName) {
        Document counter = mongoTemplate.findAndModify(
                Query.query(Criteria.where("_id").is(collectionName)),
                new Update().inc("valor", 1L),
                // upsert: si el contador no existe todavía, lo crea empezando en 1
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                Document.class, COLLECTION);
        return ((Number) counter.get("valor")).longValue();
    }
}
