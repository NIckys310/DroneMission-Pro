@echo off
REM Inicia el frontend de DroneMission Pro en http://localhost:5173 (requiere Node.js 18+)
cd /d "%~dp0frontend"
if not exist node_modules (
  echo Instalando dependencias por primera vez...
  call npm install
)
call npm run dev
