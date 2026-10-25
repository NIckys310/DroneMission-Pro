package com.dronemission.evidence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    List<Evidence> findByMissionIdOrderByDateDesc(Long missionId);

    long countByMissionId(Long missionId);
}
