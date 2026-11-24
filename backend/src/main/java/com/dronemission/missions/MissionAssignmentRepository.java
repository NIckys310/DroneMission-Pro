package com.dronemission.missions;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

// MongoRepository: Spring Data genera las consultas a MongoDB a partir del nombre del método
public interface MissionAssignmentRepository extends MongoRepository<MissionAssignment, Long> {

    List<MissionAssignment> findByDroneIdOrderByAssignedAtDesc(Long droneId);

    List<MissionAssignment> findByPilotIdAndActiveTrueOrderByAssignedAtDesc(Long pilotId);

    long countByPilotIdAndActiveTrue(Long pilotId);

    // Asignaciones vigentes de un drone (consulta base para las reglas de abajo)
    List<MissionAssignment> findByDroneIdAndActiveTrue(Long droneId);

    /*
     * MongoDB no hace JOIN entre colecciones (en SQL era "a.mission.status").
     * Por eso estas reglas traen las asignaciones vigentes del drone o del piloto
     * y revisan su misión en Java. Los servicios las siguen llamando igual que antes.
     */

    /** Asignaciones vigentes del piloto cuya misión está en el estado indicado. */
    default long countByPilotIdAndActiveTrueAndMissionStatus(Long pilotId, MissionStatus status) {
        return findByPilotIdAndActiveTrueOrderByAssignedAtDesc(pilotId).stream()
                .filter(a -> a.getMission().getStatus() == status)
                .count();
    }

    /** ¿El drone tiene otra misión activa que se cruza en fecha y horario? (regla 5) */
    default boolean droneHasOverlap(Long droneId, Long missionId, Collection<MissionStatus> statuses,
                                    LocalDate date, LocalTime start, LocalTime end) {
        return findByDroneIdAndActiveTrue(droneId).stream()
                .anyMatch(a -> overlaps(a.getMission(), missionId, statuses, date, start, end));
    }

    /** ¿El piloto tiene otra misión activa que se cruza en fecha y horario? (regla 4) */
    default boolean pilotHasOverlap(Long pilotId, Long missionId, Collection<MissionStatus> statuses,
                                    LocalDate date, LocalTime start, LocalTime end) {
        return findByPilotIdAndActiveTrueOrderByAssignedAtDesc(pilotId).stream()
                .anyMatch(a -> overlaps(a.getMission(), missionId, statuses, date, start, end));
    }

    /** ¿El drone tiene alguna misión vigente en uno de esos estados? */
    default boolean droneHasActiveMissions(Long droneId, Collection<MissionStatus> statuses) {
        return findByDroneIdAndActiveTrue(droneId).stream()
                .anyMatch(a -> statuses.contains(a.getMission().getStatus()));
    }

    /** ¿El piloto tiene alguna misión vigente en uno de esos estados? */
    default boolean pilotHasActiveMissions(Long pilotId, Collection<MissionStatus> statuses) {
        return findByPilotIdAndActiveTrueOrderByAssignedAtDesc(pilotId).stream()
                .anyMatch(a -> statuses.contains(a.getMission().getStatus()));
    }

    /** Misma regla que el JPQL original: otra misión, estado activo, mismo día y horarios que se cruzan. */
    private static boolean overlaps(Mission m, Long missionId, Collection<MissionStatus> statuses,
                                    LocalDate date, LocalTime start, LocalTime end) {
        return !m.getId().equals(missionId)
                && statuses.contains(m.getStatus())
                && m.getScheduledDate().equals(date)
                && m.getStartTime().isBefore(end)
                && m.getEstimatedEndTime().isAfter(start);
    }
}
