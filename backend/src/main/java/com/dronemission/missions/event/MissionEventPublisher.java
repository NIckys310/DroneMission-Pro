package com.dronemission.missions.event;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PATRÓN OBSERVER - Sujeto (publicador).
 * Los servicios de misiones solo llaman a publish(); no saben quién escucha.
 * Spring inyecta automáticamente todos los MissionEventListener registrados,
 * así que agregar un nuevo observador (por ejemplo, enviar correos) no exige
 * modificar MissionService (principio abierto/cerrado).
 */
@Component
public class MissionEventPublisher {

    private final List<MissionEventListener> listeners;

    public MissionEventPublisher(List<MissionEventListener> listeners) {
        this.listeners = listeners;
    }

    public void publish(MissionEvent event) {
        listeners.forEach(listener -> listener.onMissionEvent(event));
    }
}
