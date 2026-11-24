@echo off
REM Inicia el backend de DroneMission Pro (MongoDB embebido: no requiere instalar nada mas que Java 17+)
cd /d "%~dp0backend"
call mvnw.cmd spring-boot:run
