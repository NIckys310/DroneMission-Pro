package com.dronemission.users;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

// MongoRepository: Spring Data genera las consultas a MongoDB a partir del nombre del método
public interface RoleRepository extends MongoRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
