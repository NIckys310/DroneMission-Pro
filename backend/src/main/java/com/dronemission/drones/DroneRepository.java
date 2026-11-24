package com.dronemission.drones;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

// MongoRepository: Spring Data genera las consultas a MongoDB a partir del nombre del método
public interface DroneRepository extends MongoRepository<Drone, Long> {

    List<Drone> findAllByOrderByCodeAsc();

    List<Drone> findByActiveTrue();

    long countByActiveTrue();

    long countByActiveTrueAndStatus(DroneStatus status);

    List<Drone> findByActiveTrueAndNextMaintenanceOnLessThanEqual(LocalDate date);

    boolean existsByCode(String code);

    boolean existsBySerialNumber(String serialNumber);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);
}
