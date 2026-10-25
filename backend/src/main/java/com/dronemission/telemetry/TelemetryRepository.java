package com.dronemission.telemetry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {

    List<Telemetry> findByMissionIdOrderByIdAsc(Long missionId);

    List<Telemetry> findByMissionIdAndIdGreaterThanOrderByIdAsc(Long missionId, Long afterId);

    Optional<Telemetry> findTopByMissionIdOrderByIdDesc(Long missionId);

    Optional<Telemetry> findTopByMissionIdOrderByIdAsc(Long missionId);
}
