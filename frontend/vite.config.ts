import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

// En desarrollo, Vite redirige /api y /demo al backend (evita problemas de CORS).
const backend = process.env.VITE_BACKEND_URL ?? 'http://localhost:8080';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': backend,
      '/demo': backend,
    },
  },
  build: {
    rollupOptions: {
      output: {
        // Separa las librerías grandes en archivos propios (mejor caché del navegador)
        manualChunks: {
          react: ['react', 'react-dom', 'react-router-dom'],
          mui: ['@mui/material', '@mui/icons-material', '@emotion/react', '@emotion/styled'],
          charts: ['recharts'],
        },
      },
    },
  },
  test: {
    environment: 'node',
  },
});
