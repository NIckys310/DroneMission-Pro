package com.dronemission.missions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface MissionAssignmentRepository extends JpaRepository<MissionAssignment, Long> {

    List<MissionAssignment> findByDroneIdOrderByAssignedAtDesc(Long droneId);

    List<MissionAssignment> findByPilotIdAndActiveTrueOrderByAssignedAtDesc(Long pilotId);

    long countByPilotIdAndActiveTrue(Long pilotId);

    long countByPilotIdAndActiveTrueAndMissionStatus(Long pilotId, MissionStatus status);

    /** ¿El drone tiene otra misión activa que se cruza en fecha y horario? (regla 5) */
    @Query("""
            select count(a) > 0 from MissionAssignment a
            where a.active = true and a.drone.id = :droneId and a.mission.id <> :missionId
              and a.mission.status in :statuses and a.mission.scheduledDate = :date
              and a.mission.startTime < :end and a.mission.estimatedEndTime > :start
            """)
    boolean droneHasOverlap(@Param("droneId") Long droneId, @Param("missionId") Long missionId,
                            @Param("statuses") Collection<MissionStatus> statuses, @Param("date") LocalDate date,
                            @Param("start") LocalTime start, @Param("end") LocalTime end);

    /** ¿El piloto tiene otra misión activa que se cruza en fecha y horario? (regla 4) */
    @Query("""
            select count(a) > 0 from MissionAssignment a
            where a.active = true and a.pilot.id = :pilotId and a.mission.id <> :missionId
              and a.mission.status in :statuses and a.mission.scheduledDate = :date
              and a.mission.startTime < :end and a.mission.estimatedEndTime > :start
            """)
    boolean pilotHasOverlap(@Param("pilotId") Long pilotId, @Param("missionId") Long missionId,
                            @Param("statuses") Collection<MissionStatus> statuses, @Param("date") LocalDate date,
                            @Param("start") LocalTime start, @Param("end") LocalTime end);

    @Query("""
            select count(a) > 0 from MissionAssignment a
            where a.active = true and a.drone.id = :droneId and a.mission.status in :statuses
            """)
    boolean droneHasActiveMissions(@Param("droneId") Long droneId, @Param("statuses") Collection<MissionStatus> statuses);

    @Query("""
            select count(a) > 0 from MissionAssignment a
            where a.active = true and a.pilot.id = :pilotId and a.mission.status in :statuses
            """)
    boolean pilotHasActiveMissions(@Param("pilotId") Long pilotId, @Param("statuses") Collection<MissionStatus> statuses);
}
