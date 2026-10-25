package com.dronemission.missions;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * MÁQUINA DE ESTADOS de una misión.
 * Cada estado conoce a qué estados puede pasar. Cualquier otra transición es inválida.
 *
 *   BORRADOR -> PENDIENTE -> PROGRAMADA -> EN_PREPARACION -> EN_VUELO <-> PAUSADA
 *                                                              |            |
 *                                                              +-> FINALIZADA <-+
 *   BORRADOR / PENDIENTE / PROGRAMADA / EN_PREPARACION -> CANCELADA
 */
public enum MissionStatus {
    BORRADOR, PENDIENTE, PROGRAMADA, EN_PREPARACION, EN_VUELO, PAUSADA, FINALIZADA, CANCELADA;

    private static final Map<MissionStatus, Set<MissionStatus>> TRANSITIONS = new EnumMap<>(MissionStatus.class);

    static {
        TRANSITIONS.put(BORRADOR, EnumSet.of(PENDIENTE, CANCELADA));
        TRANSITIONS.put(PENDIENTE, EnumSet.of(PROGRAMADA, CANCELADA));
        TRANSITIONS.put(PROGRAMADA, EnumSet.of(EN_PREPARACION, CANCELADA));
        TRANSITIONS.put(EN_PREPARACION, EnumSet.of(EN_VUELO, CANCELADA));
        TRANSITIONS.put(EN_VUELO, EnumSet.of(PAUSADA, FINALIZADA));
        TRANSITIONS.put(PAUSADA, EnumSet.of(EN_VUELO, FINALIZADA));
        TRANSITIONS.put(FINALIZADA, EnumSet.noneOf(MissionStatus.class));
        TRANSITIONS.put(CANCELADA, EnumSet.noneOf(MissionStatus.class));
    }

    public boolean canTransitionTo(MissionStatus target) {
        return TRANSITIONS.get(this).contains(target);
    }

    public Set<MissionStatus> allowedTransitions() {
        return EnumSet.copyOf(TRANSITIONS.get(this));
    }

    /** Datos de la misión editables solo antes del vuelo. */
    public boolean isEditable() {
        return this == BORRADOR || this == PENDIENTE || this == PROGRAMADA;
    }

    /** Estados en los que la misión "ocupa" su drone y su piloto en la agenda. */
    public static Set<MissionStatus> occupyingResources() {
        return EnumSet.of(PROGRAMADA, EN_PREPARACION, EN_VUELO, PAUSADA);
    }

    /** Estados en los que el drone está físicamente en operación. */
    public boolean isInOperation() {
        return this == EN_PREPARACION || this == EN_VUELO || this == PAUSADA;
    }

    public boolean isTerminal() {
        return this == FINALIZADA || this == CANCELADA;
    }
}
