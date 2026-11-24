package com.dronemission.evidence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

// MongoRepository: Spring Data genera las consultas a MongoDB a partir del nombre del método
public interface EvidenceRepository extends MongoRepository<Evidence, Long> {

    List<Evidence> findByMissionIdOrderByDateDesc(Long missionId);

    long countByMissionId(Long missionId);
}
