import axios, { AxiosError } from 'axios';
import type { ApiError } from '../types';

export const TOKEN_KEY = 'dronemission.token';

/**
 * Cliente HTTP único. Agrega el token JWT a cada petición y, si el backend
 * responde 401 (token vencido), cierra la sesión y vuelve al login.
 */
export const http = axios.create({
  baseURL: '/api',
  timeout: 20000,
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    const isLogin = error.config?.url?.includes('/auth/login');
    if (error.response?.status === 401 && !isLogin) {
      localStorage.removeItem(TOKEN_KEY);
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login?expired=1';
      }
    }
    return Promise.reject(error);
  },
);

/** Extrae el mensaje de error que envía el backend (formato ApiError). */
export function errorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as ApiError | undefined;
    if (data?.message) return data.message;
    if (!error.response) return 'No hay conexión con el servidor. ¿Está encendido el backend?';
    return `Error ${error.response.status}`;
  }
  return 'Ocurrió un error inesperado';
}

/** Errores por campo (validaciones del backend). */
export function fieldErrors(error: unknown): Record<string, string> {
  if (axios.isAxiosError(error)) {
    return (error.response?.data as ApiError | undefined)?.fieldErrors ?? {};
  }
  return {};
}

/** Descarga un archivo protegido (PDF, evidencia) usando el token. */
export async function downloadFile(url: string, fallbackName: string): Promise<void> {
  const response = await http.get(url, { responseType: 'blob' });
  const disposition = response.headers['content-disposition'] as string | undefined;
  const match = disposition?.match(/filename="?([^"]+)"?/);
  const href = URL.createObjectURL(response.data as Blob);
  const link = document.createElement('a');
  link.href = href;
  link.download = match?.[1] ?? fallbackName;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(href);
}
