package com.dronemission.clients;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findAllByOrderByNameAsc();

    Optional<Client> findByUserId(Long userId);

    boolean existsByDocument(String document);

    boolean existsByDocumentAndIdNot(String document, Long id);
}
