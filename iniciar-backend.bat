@echo off
REM Inicia el backend de DroneMission Pro (base de datos H2, no requiere instalar nada mas que Java 17+)
cd /d "%~dp0backend"
call mvnw.cmd spring-boot:run
