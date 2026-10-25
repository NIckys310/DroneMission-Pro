package com.dronemission.audit;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByDateDesc(Pageable pageable);

    List<AuditLog> findByEntityAndEntityIdOrderByDateAsc(String entity, Long entityId);
}
