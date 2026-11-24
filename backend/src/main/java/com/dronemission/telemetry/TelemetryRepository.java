package com.dronemission.telemetry;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

// MongoRepository: Spring Data genera las consultas a MongoDB a partir del nombre del método
public interface TelemetryRepository extends MongoRepository<Telemetry, Long> {

    List<Telemetry> findByMissionIdOrderByIdAsc(Long missionId);

    List<Telemetry> findByMissionIdAndIdGreaterThanOrderByIdAsc(Long missionId, Long afterId);

    Optional<Telemetry> findTopByMissionIdOrderByIdDesc(Long missionId);

    Optional<Telemetry> findTopByMissionIdOrderByIdAsc(Long missionId);
}
