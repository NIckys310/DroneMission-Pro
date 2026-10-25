package com.dronemission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de DroneMission Pro.
 * @EnableScheduling activa las tareas programadas (simulador de telemetría
 * y verificación diaria de licencias).
 */
@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class DroneMissionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DroneMissionApplication.class, args);
    }
}
