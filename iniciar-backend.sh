#!/usr/bin/env bash
# Inicia el backend de DroneMission Pro (MongoDB embebido por defecto)
cd "$(dirname "$0")/backend" && ./mvnw spring-boot:run
