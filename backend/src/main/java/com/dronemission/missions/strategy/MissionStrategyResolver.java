package com.dronemission.missions.strategy;

import com.dronemission.missions.MissionType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Contexto del patrón Strategy: devuelve la estrategia adecuada para un tipo de misión.
 * Spring inyecta todas las implementaciones de MissionStrategy; agregar un nuevo
 * comportamiento solo requiere crear una nueva clase @Component.
 */
@Component
public class MissionStrategyResolver {

    private final Map<MissionType, MissionStrategy> strategies = new EnumMap<>(MissionType.class);

    public MissionStrategyResolver(List<MissionStrategy> available) {
        for (MissionStrategy strategy : available) {
            strategy.supportedTypes().forEach(type -> strategies.put(type, strategy));
        }
        for (MissionType type : MissionType.values()) {
            if (!strategies.containsKey(type)) {
                throw new IllegalStateException("No hay una estrategia para el tipo de misión " + type);
            }
        }
    }

    public MissionStrategy forType(MissionType type) {
        return strategies.get(type);
    }
}
