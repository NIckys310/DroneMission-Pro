package com.dronemission.telemetry;

import com.dronemission.drones.Drone;
import com.dronemission.incidents.IncidentService;
import com.dronemission.incidents.IncidentSeverity;
import com.dronemission.incidents.IncidentType;
import com.dronemission.missions.*;
import com.dronemission.missions.strategy.GeoPoint;
import com.dronemission.missions.strategy.MissionStrategy;
import com.dronemission.missions.strategy.MissionStrategyResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * DRONE VIRTUAL: simulador de telemetría.
 *
 * No hay un drone físico. Cada "tick" (por defecto cada 2 s) el simulador toma
 * las misiones EN_VUELO y mueve su drone virtual hacia el siguiente punto del
 * plan de vuelo programado, calculando posición, altitud, velocidad, batería,
 * distancia, temperatura y tiempo. Cada lectura se guarda en la tabla telemetria
 * marcada como simulada. Si la misión se pausa, el drone deja de avanzar.
 *
 * Es un bean Singleton de Spring: existe un único motor de simulación compartido.
 * El estado NO se guarda en memoria: se reconstruye desde la última lectura,
 * así la simulación continúa aunque se reinicie el servidor.
 *
 * time-factor acelera el tiempo simulado para que una demo dure pocos minutos.
 */
@Slf4j
@Component
public class TelemetrySimulator {

    private static final int LOW_BATTERY_THRESHOLD = 20;
    private static final double CLIMB_RATE_MS = 3.0;
    private static final double AMBIENT_TEMPERATURE_C = 24.0;

    private final MissionRepository missionRepository;
    private final WaypointRepository waypointRepository;
    private final TelemetryRepository telemetryRepository;
    private final MissionStrategyResolver strategyResolver;
    private final IncidentService incidentService;
    private final double secondsPerTick;
    private final Random random = new Random();

    public TelemetrySimulator(MissionRepository missionRepository, WaypointRepository waypointRepository,
                              TelemetryRepository telemetryRepository, MissionStrategyResolver strategyResolver,
                              IncidentService incidentService,
                              @Value("${app.simulation.tick-ms}") long tickMs,
                              @Value("${app.simulation.time-factor}") double timeFactor) {
        this.missionRepository = missionRepository;
        this.waypointRepository = waypointRepository;
        this.telemetryRepository = telemetryRepository;
        this.strategyResolver = strategyResolver;
        this.incidentService = incidentService;
        this.secondsPerTick = tickMs / 1000.0 * timeFactor;
    }

    @Scheduled(fixedRateString = "${app.simulation.tick-ms}", initialDelay = 5000)
    @Transactional
    public void tick() {
        for (Mission mission : missionRepository.findByStatus(MissionStatus.EN_VUELO)) {
            try {
                simulateStep(mission);
            } catch (RuntimeException e) {
                log.warn("No se pudo simular la misión {}: {}", mission.getCode(), e.getMessage());
            }
        }
    }

    /** Avanza el drone virtual de una misión un paso de simulación. */
    void simulateStep(Mission mission) {
        List<Waypoint> plan = waypointRepository.findByMissionIdOrderBySequenceAsc(mission.getId());
        Optional<Drone> droneOpt = mission.getActiveAssignment().map(MissionAssignment::getDrone);
        if (plan.size() < 2 || droneOpt.isEmpty()) {
            return;
        }
        Drone drone = droneOpt.get();
        MissionStrategy strategy = strategyResolver.forType(mission.getType());
        Telemetry last = telemetryRepository.findTopByMissionIdOrderByIdDesc(mission.getId()).orElse(null);

        // Estado actual del drone virtual (o estado inicial en el punto de despegue)
        GeoPoint position = last == null ? FlightPlanService.toPoint(plan.get(0)) : new GeoPoint(last.getLatitude(), last.getLongitude());
        double altitude = last == null ? 0 : last.getAltitudeM();
        double distance = last == null ? 0 : last.getDistanceM();
        int flightTime = last == null ? 0 : last.getFlightTimeS();
        int targetIndex = last == null ? 1 : last.getCurrentWaypoint();
        int startBattery = telemetryRepository.findTopByMissionIdOrderByIdAsc(mission.getId())
                .map(Telemetry::getBattery).orElse(drone.getBatteryLevel());

        if (targetIndex >= plan.size()) {
            return; // ruta completada: el drone espera en tierra a que el piloto finalice
        }

        // Movimiento horizontal hacia el siguiente punto del plan
        Waypoint target = plan.get(targetIndex);
        GeoPoint targetPoint = FlightPlanService.toPoint(target);
        double speedMs = strategy.cruiseSpeedKmh() / 3.6 * (0.9 + random.nextDouble() * 0.2);
        double step = speedMs * secondsPerTick;
        double remaining = position.distanceTo(targetPoint);
        GeoPoint next;
        if (step >= remaining) {
            next = targetPoint;
            step = remaining;
            targetIndex++;
        } else {
            next = position.interpolate(targetPoint, step / remaining);
        }

        // Movimiento vertical: sube o baja hacia la altitud del punto objetivo
        double maxClimb = CLIMB_RATE_MS * secondsPerTick;
        double altitudeDelta = Math.max(-maxClimb, Math.min(maxClimb, target.getAltitudeM() - altitude));
        double newAltitude = Math.max(0, altitude + altitudeDelta + (target.getAltitudeM() > 0 ? random.nextGaussian() * 0.3 : 0));
        boolean landed = targetIndex >= plan.size();
        if (landed) {
            newAltitude = 0;
        }

        int newFlightTime = flightTime + (int) Math.round(secondsPerTick);
        int battery = (int) Math.max(0, Math.round(startBattery - strategy.batteryDrainPerMinute() * newFlightTime / 60.0));
        double temperature = AMBIENT_TEMPERATURE_C + 6.0 * newFlightTime / 600.0 + random.nextGaussian() * 0.2;

        Telemetry reading = new Telemetry();
        reading.setMission(mission);
        reading.setLatitude(round6(next.latitude()));
        reading.setLongitude(round6(next.longitude()));
        reading.setAltitudeM(round2(newAltitude));
        reading.setSpeedKmh(landed ? 0 : round2(step / secondsPerTick * 3.6));
        reading.setBattery(battery);
        reading.setDistanceM(round2(distance + step));
        reading.setTemperatureC(round2(temperature));
        reading.setFlightTimeS(newFlightTime);
        reading.setCurrentWaypoint(targetIndex);
        reading.setSimulated(true);
        telemetryRepository.save(reading);

        int previousBattery = last == null ? startBattery : last.getBattery();
        if (previousBattery > LOW_BATTERY_THRESHOLD && battery <= LOW_BATTERY_THRESHOLD) {
            incidentService.registerSystemIncident(mission, IncidentType.BATERIA_BAJA, IncidentSeverity.ALTA,
                    "Batería del drone " + drone.getCode() + " en " + battery + " % (alerta automática del sistema).");
        }
    }

    private static double round6(double v) {
        return Math.round(v * 1_000_000d) / 1_000_000d;
    }

    private static double round2(double v) {
        return Math.round(v * 100d) / 100d;
    }
}
