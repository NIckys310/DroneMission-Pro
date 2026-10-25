package com.dronemission.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByResetToken(String resetToken);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findAllByOrderByIdAsc();

    List<User> findByRoleNameAndActiveTrue(RoleName roleName);
}
