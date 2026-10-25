package com.dronemission.drones;

import com.dronemission.drones.dto.DroneRequest;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * PATRÓN FACTORY (método fábrica).
 * Centraliza la creación de drones: según el TIPO aplica valores por defecto
 * realistas (capacidad de batería, velocidad máxima y autonomía) cuando el
 * usuario no los indica, y genera el código interno DRN-XXX.
 * Así el servicio no necesita conocer los detalles de cada tipo de drone.
 */
@Component
public class DroneFactory {

    /** Especificación típica de cada tipo de drone. */
    record DroneSpec(int batteryCapacityMah, int maxSpeedKmh, int autonomyMinutes) {
    }

    private static final Map<DroneType, DroneSpec> SPECS = new EnumMap<>(DroneType.class);

    static {
        SPECS.put(DroneType.INSPECCION,  new DroneSpec(5935, 80, 50));
        SPECS.put(DroneType.AGRICULTURA, new DroneSpec(30000, 36, 18));
        SPECS.put(DroneType.VIGILANCIA,  new DroneSpec(8000, 70, 40));
        SPECS.put(DroneType.TOPOGRAFIA,  new DroneSpec(5800, 60, 35));
        SPECS.put(DroneType.FOTOGRAFIA,  new DroneSpec(5000, 75, 43));
    }

    /** Crea un drone nuevo a partir de la solicitud, completando lo que falte según su tipo. */
    public Drone createDrone(DroneRequest request, long nextSequence) {
        Drone drone = new Drone();
        DroneMapper.apply(request, drone);

        DroneSpec spec = SPECS.get(request.type());
        if (drone.getBatteryCapacityMah() == null) drone.setBatteryCapacityMah(spec.batteryCapacityMah());
        if (drone.getMaxSpeedKmh() == null) drone.setMaxSpeedKmh(spec.maxSpeedKmh());
        if (drone.getAutonomyMinutes() == null) drone.setAutonomyMinutes(spec.autonomyMinutes());

        String code = request.code() == null || request.code().isBlank()
                ? String.format("DRN-%03d", nextSequence)
                : request.code().trim().toUpperCase();
        drone.setCode(code);
        drone.setStatus(DroneStatus.DISPONIBLE);
        drone.setActive(true);
        return drone;
    }

    public static DroneSpec specOf(DroneType type) {
        return SPECS.get(type);
    }
}
