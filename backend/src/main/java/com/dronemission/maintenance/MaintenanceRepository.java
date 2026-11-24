package com.dronemission.maintenance;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

// MongoRepository: Spring Data genera las consultas a MongoDB a partir del nombre del método
public interface MaintenanceRepository extends MongoRepository<Maintenance, Long> {

    List<Maintenance> findAllByOrderByDateDesc();

    List<Maintenance> findByDroneIdOrderByDateDesc(Long droneId);

    List<Maintenance> findByStatusInOrderByDateAsc(Collection<MaintenanceStatus> statuses);

    boolean existsByDroneIdAndStatusAndIdNot(Long droneId, MaintenanceStatus status, Long id);

    boolean existsByDroneIdAndStatus(Long droneId, MaintenanceStatus status);
}
