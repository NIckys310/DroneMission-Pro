package com.dronemission.users;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

// MongoRepository: Spring Data genera las consultas a MongoDB a partir del nombre del método
public interface PermissionRepository extends MongoRepository<Permission, Long> {

    List<Permission> findAllByOrderByIdAsc();
}
