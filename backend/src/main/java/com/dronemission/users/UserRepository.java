package com.dronemission.users;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

// MongoRepository: Spring Data genera las consultas a MongoDB a partir del nombre del método
public interface UserRepository extends MongoRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByResetToken(String resetToken);

    boolean existsByUsernameIgnoreCase(String username);

    List<User> findByEmailEndingWithIgnoreCase(String suffix);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findAllByOrderByIdAsc();

    List<User> findByActiveTrue();

    // El rol es otro documento (DBRef): se filtra en Java en lugar de "role.name"
    default List<User> findByRoleNameAndActiveTrue(RoleName roleName) {
        return findByActiveTrue().stream().filter(u -> u.getRole().getName() == roleName).toList();
    }
}
