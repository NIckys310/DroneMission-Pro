package com.dronemission.missions;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

// MongoRepository: Spring Data genera las consultas a MongoDB a partir del nombre del método
public interface MissionRepository extends MongoRepository<Mission, Long> {

    List<Mission> findAllByOrderByScheduledDateDescStartTimeDesc();

    List<Mission> findByStatus(MissionStatus status);

    long countByStatus(MissionStatus status);

    long countByClientId(Long clientId);

    boolean existsByClientIdAndStatusIn(Long clientId, Collection<MissionStatus> statuses);

    List<Mission> findByClientIdOrderByScheduledDateDesc(Long clientId);

    // Rango inclusivo (como BETWEEN de SQL). "fecha_programada" es el nombre del campo en MongoDB
    @Query("{ 'fecha_programada': { $gte: ?0, $lte: ?1 } }")
    List<Mission> findByScheduledDateBetween(LocalDate from, LocalDate to);

    Optional<Mission> findTopByOrderByIdDesc();

    // Misiones sin plan de vuelo: el arreglo embebido "plan_vuelo" no existe o está vacío
    @Query("{ $or: [ { 'plan_vuelo': { $exists: false } }, { 'plan_vuelo': { $size: 0 } } ] }")
    List<Mission> findWithoutFlightPlan();
}
