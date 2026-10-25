// Service worker de DroneMission Pro: permite instalar la aplicación y abrirla rápido.
// IMPORTANTE: nunca guarda en caché la API (/api): los datos siempre vienen del servidor.
const CACHE = 'dronemission-v1';
const SHELL = ['/', '/index.html', '/manifest.json', '/drone.svg', '/icons/icon-192.png', '/icons/icon-512.png'];

self.addEventListener('install', (event) => {
  event.waitUntil(caches.open(CACHE).then((cache) => cache.addAll(SHELL)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k))))
      .then(() => self.clients.claim()),
  );
});

self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);
  if (request.method !== 'GET' || url.origin !== self.location.origin || url.pathname.startsWith('/api/')) {
    return; // la API y los demás orígenes van directo a la red
  }
  // Pantallas: primero la red (siempre la versión nueva); sin conexión, la copia guardada
  if (request.mode === 'navigate') {
    event.respondWith(fetch(request).catch(() => caches.match('/index.html')));
    return;
  }
  // Archivos estáticos con hash (/assets/...): primero la caché, son inmutables
  if (url.pathname.startsWith('/assets/') || SHELL.includes(url.pathname)) {
    event.respondWith(
      caches.match(request).then((cached) => cached || fetch(request).then((response) => {
        const copy = response.clone();
        if (response.ok) caches.open(CACHE).then((cache) => cache.put(request, copy));
        return response;
      })),
    );
  }
});
