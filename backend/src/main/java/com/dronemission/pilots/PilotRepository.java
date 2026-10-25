package com.dronemission.pilots;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PilotRepository extends JpaRepository<Pilot, Long> {

    List<Pilot> findAllByOrderByFirstNameAsc();

    Optional<Pilot> findByUserId(Long userId);

    List<Pilot> findByActiveTrue();

    long countByActiveTrue();

    long countByActiveTrueAndStatus(PilotStatus status);

    List<Pilot> findByActiveTrueAndLicenseExpiresOnBetween(LocalDate from, LocalDate to);

    boolean existsByDocument(String document);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByDocumentAndIdNot(String document, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByLicenseNumberAndIdNot(String licenseNumber, Long id);
}
