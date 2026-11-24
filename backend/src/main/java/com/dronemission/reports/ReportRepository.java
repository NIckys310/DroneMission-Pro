package com.dronemission.reports;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

// MongoRepository: Spring Data genera las consultas a MongoDB a partir del nombre del método
public interface ReportRepository extends MongoRepository<Report, Long> {

    List<Report> findAllByOrderByGeneratedAtDesc();

    // MongoDB no cruza report -> misión -> cliente en una sola consulta: se filtra en Java
    default List<Report> findByMissionClientIdOrderByGeneratedAtDesc(Long clientId) {
        return findAllByOrderByGeneratedAtDesc().stream()
                .filter(r -> r.getMission() != null && clientId.equals(r.getMission().getClient().getId()))
                .toList();
    }
}
