#!/usr/bin/env bash
# Inicia el backend de DroneMission Pro (base de datos H2 por defecto)
cd "$(dirname "$0")/backend" && ./mvnw spring-boot:run
