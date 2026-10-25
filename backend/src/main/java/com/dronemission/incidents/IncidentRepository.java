package com.dronemission.incidents;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findAllByOrderByDateDesc();

    List<Incident> findAllByOrderByDateDesc(Pageable pageable);

    List<Incident> findByMissionIdOrderByDateDesc(Long missionId);

    boolean existsByMissionIdAndType(Long missionId, IncidentType type);

    long countByStatusNot(IncidentStatus status);

    long countBySeverityAndStatusNot(IncidentSeverity severity, IncidentStatus status);
}
