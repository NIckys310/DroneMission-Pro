package com.dronemission.maintenance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    List<Maintenance> findAllByOrderByDateDesc();

    List<Maintenance> findByDroneIdOrderByDateDesc(Long droneId);

    List<Maintenance> findByStatusInOrderByDateAsc(Collection<MaintenanceStatus> statuses);

    boolean existsByDroneIdAndStatusAndIdNot(Long droneId, MaintenanceStatus status, Long id);

    boolean existsByDroneIdAndStatus(Long droneId, MaintenanceStatus status);
}
