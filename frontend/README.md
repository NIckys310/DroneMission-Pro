# DroneMission Pro — Frontend

**React 18 + TypeScript + Vite + Material UI.**

```bash
npm install
npm run dev       # http://localhost:5173 (redirige /api al backend en :8080)
npm run build     # verifica tipos y genera dist/
npm test          # pruebas de validación (Vitest)
```

## Estructura

| Carpeta | Contenido |
|---|---|
| `src/api` | `client.ts` (Axios + token JWT + errores) y `endpoints.ts` (todos los endpoints del backend) |
| `src/auth` | `AuthContext` (sesión y permisos) y `ProtectedRoute` |
| `src/components` | Layout, formulario genérico `EntityFormDialog`, chips de estado, batería, mapa de vuelo… |
| `src/pages` | Una carpeta por módulo (drones, pilotos, misiones…) |
| `src/types` | Tipos que reflejan los DTO del backend |
| `src/utils` | Etiquetas en español, formatos y validaciones |

Para agregar un campo a un formulario basta con añadir una línea al arreglo de campos de la página (por ejemplo, `pages/drones/droneForm.ts`). Ver `docs/guia-modificaciones.md`.
