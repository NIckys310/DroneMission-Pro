package com.dronemission.config;

import com.dronemission.common.persistence.MongoUnitOfWorkTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuración de MongoDB.
 *
 * La conexión (embebida o MONGODB_URI) la arma Spring Boot con application.yml.
 * Aquí solo se registra el administrador de transacciones, para que los @Transactional
 * de los servicios sigan funcionando igual que con JPA.
 */
@Configuration
public class MongoConfig {

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(MongoTemplate mongoTemplate) {
        return new MongoUnitOfWorkTransactionManager(mongoTemplate);
    }
}
