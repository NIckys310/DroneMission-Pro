package com.dronemission.notifications;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

// MongoRepository: Spring Data genera las consultas a MongoDB a partir del nombre del método
public interface NotificationRepository extends MongoRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByDateDesc(Long userId, Pageable pageable);

    List<Notification> findByUserIdAndReadFalse(Long userId);

    long countByUserIdAndReadFalse(Long userId);
}
