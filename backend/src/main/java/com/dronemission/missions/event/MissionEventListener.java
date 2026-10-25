package com.dronemission.missions.event;

/**
 * PATRÓN OBSERVER - Observador.
 * Cualquier componente interesado en los cambios de una misión implementa esta
 * interfaz. Hoy existen dos observadores: auditoría y notificaciones.
 */
public interface MissionEventListener {

    void onMissionEvent(MissionEvent event);
}
