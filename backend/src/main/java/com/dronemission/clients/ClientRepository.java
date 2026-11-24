package com.dronemission.clients;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

// MongoRepository: Spring Data genera las consultas a MongoDB a partir del nombre del método
public interface ClientRepository extends MongoRepository<Client, Long> {

    List<Client> findAllByOrderByNameAsc();

    Optional<Client> findByUserId(Long userId);

    boolean existsByDocument(String document);

    boolean existsByDocumentAndIdNot(String document, Long id);
}
