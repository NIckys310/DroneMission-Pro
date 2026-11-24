package com.dronemission.pilots;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// MongoRepository: Spring Data genera las consultas a MongoDB a partir del nombre del método
public interface PilotRepository extends MongoRepository<Pilot, Long> {

    List<Pilot> findAllByOrderByFirstNameAsc();

    Optional<Pilot> findByUserId(Long userId);

    List<Pilot> findByActiveTrue();

    long countByActiveTrue();

    long countByActiveTrueAndStatus(PilotStatus status);

    // Rango inclusivo de fechas de vencimiento ("Between" de Mongo excluiría los extremos)
    @Query("{ 'activo': true, 'vencimiento_licencia': { $gte: ?0, $lte: ?1 } }")
    List<Pilot> findByActiveTrueAndLicenseExpiresOnBetween(LocalDate from, LocalDate to);

    boolean existsByDocument(String document);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByDocumentAndIdNot(String document, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByLicenseNumberAndIdNot(String licenseNumber, Long id);
}
