package com.dronemission.drones;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DroneRepository extends JpaRepository<Drone, Long> {

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
