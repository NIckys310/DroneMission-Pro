#!/usr/bin/env bash
# Inicia el frontend de DroneMission Pro en http://localhost:5173
cd "$(dirname "$0")/frontend"
[ -d node_modules ] || npm install
npm run dev
