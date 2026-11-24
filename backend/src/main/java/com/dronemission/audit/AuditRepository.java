package com.dronemission.audit;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

// MongoRepository: Spring Data genera las consultas a MongoDB a partir del nombre del método
public interface AuditRepository extends MongoRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByDateDesc(Pageable pageable);

    List<AuditLog> findByEntityAndEntityIdOrderByDateAsc(String entity, Long entityId);
}
