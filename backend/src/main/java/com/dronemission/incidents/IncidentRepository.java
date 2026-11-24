package com.dronemission.incidents;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

// MongoRepository: Spring Data genera las consultas a MongoDB a partir del nombre del método
public interface IncidentRepository extends MongoRepository<Incident, Long> {

    List<Incident> findAllByOrderByDateDesc();

    List<Incident> findAllByOrderByDateDesc(Pageable pageable);

    List<Incident> findByMissionIdOrderByDateDesc(Long missionId);

    boolean existsByMissionIdAndType(Long missionId, IncidentType type);

    long countByStatusNot(IncidentStatus status);

    long countBySeverityAndStatusNot(IncidentSeverity severity, IncidentStatus status);
}
