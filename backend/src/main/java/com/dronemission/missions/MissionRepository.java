package com.dronemission.missions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    List<Mission> findAllByOrderByScheduledDateDescStartTimeDesc();

    List<Mission> findByStatus(MissionStatus status);

    long countByStatus(MissionStatus status);

    long countByClientId(Long clientId);

    boolean existsByClientIdAndStatusIn(Long clientId, Collection<MissionStatus> statuses);

    List<Mission> findByClientIdOrderByScheduledDateDesc(Long clientId);

    List<Mission> findByScheduledDateBetween(LocalDate from, LocalDate to);

    Optional<Mission> findTopByOrderByIdDesc();

    @Query("select m from Mission m where m.flightPlan is empty")
    List<Mission> findWithoutFlightPlan();
}
