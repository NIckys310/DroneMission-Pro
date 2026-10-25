package com.dronemission.missions.event;

/** Eventos relevantes en la vida de una misión (cada uno se audita y puede notificar). */
public enum MissionEventType {
    CREADA, ACTUALIZADA, ENVIADA, ASIGNADA, PREPARADA, INICIADA, PAUSADA, REANUDADA,
    FINALIZADA, CANCELADA, ELIMINADA, INCIDENTE_CRITICO
}
