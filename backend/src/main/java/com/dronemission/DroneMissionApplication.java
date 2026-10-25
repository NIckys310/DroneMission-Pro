package com.dronemission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de DroneMission Pro.
 * @EnableScheduling activa las tareas programadas (simulador de telemetría
 * y verificación diaria de licencias).
 */
@SpringBootApplication
@EnableScheduling
public class DroneMissionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DroneMissionApplication.class, args);
    }
}
