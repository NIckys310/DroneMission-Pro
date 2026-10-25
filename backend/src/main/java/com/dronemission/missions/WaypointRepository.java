package com.dronemission.missions;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WaypointRepository extends JpaRepository<Waypoint, Long> {

    List<Waypoint> findByMissionIdOrderBySequenceAsc(Long missionId);
}
