package com.dronemission.telemetry;

import com.dronemission.missions.Mission;
import com.dronemission.missions.MissionAccessService;
import com.dronemission.missions.MissionService;
import com.dronemission.missions.MissionStatus;
import com.dronemission.missions.WaypointRepository;
import com.dronemission.telemetry.dto.TelemetryFeed;
import com.dronemission.telemetry.dto.TelemetryPoint;
import com.dronemission.telemetry.dto.TelemetrySummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Consultas de telemetría (los datos los genera TelemetrySimulator). */
@Service
@RequiredArgsConstructor
public class TelemetryService {

    public static final String SIMULATION_NOTICE =
            "DATOS SIMULADOS DE DEMOSTRACIÓN: no provienen de un drone real.";

    private final TelemetryRepository telemetryRepository;
    private final WaypointRepository waypointRepository;
    private final MissionService missionService;
    private final MissionAccessService accessService;

    @Transactional(readOnly = true)
    public TelemetryFeed feed(Long missionId) {
        Mission mission = missionService.get(missionId);
        accessService.checkView(mission);
        List<Telemetry> readings = telemetryRepository.findByMissionIdOrderByIdAsc(missionId);
        int totalWaypoints = waypointRepository.findByMissionIdOrderBySequenceAsc(missionId).size();
        boolean routeCompleted = !readings.isEmpty() && readings.get(readings.size() - 1).getCurrentWaypoint() >= totalWaypoints;
        boolean live = mission.getStatus() == MissionStatus.EN_VUELO && !routeCompleted;
        return new TelemetryFeed(true, SIMULATION_NOTICE, mission.getStatus().name(), live, routeCompleted,
                totalWaypoints, summarize(readings), readings.stream().map(TelemetryService::toPoint).toList());
    }

    @Transactional(readOnly = true)
    public TelemetrySummary summarize(Long missionId) {
        return summarize(telemetryRepository.findByMissionIdOrderByIdAsc(missionId));
    }

    public static TelemetrySummary summarize(List<Telemetry> readings) {
        if (readings.isEmpty()) {
            return TelemetrySummary.empty();
        }
        Telemetry first = readings.get(0);
        Telemetry last = readings.get(readings.size() - 1);
        double maxAlt = readings.stream().mapToDouble(Telemetry::getAltitudeM).max().orElse(0);
        double maxSpeed = readings.stream().mapToDouble(Telemetry::getSpeedKmh).max().orElse(0);
        double avgSpeed = readings.stream().mapToDouble(Telemetry::getSpeedKmh).filter(v -> v > 0).average().orElse(0);
        double maxTemp = readings.stream().mapToDouble(Telemetry::getTemperatureC).max().orElse(0);
        return new TelemetrySummary(readings.size(), round(maxAlt), round(avgSpeed), round(maxSpeed),
                round(last.getDistanceM()), last.getFlightTimeS(), first.getBattery(), last.getBattery(),
                Math.max(0, first.getBattery() - last.getBattery()), round(maxTemp));
    }

    private static TelemetryPoint toPoint(Telemetry t) {
        return new TelemetryPoint(t.getId(), t.getRecordedAt(), t.getLatitude(), t.getLongitude(), t.getAltitudeM(),
                t.getSpeedKmh(), t.getBattery(), t.getDistanceM(), t.getTemperatureC(), t.getFlightTimeS(),
                t.getCurrentWaypoint(), t.isSimulated());
    }

    private static double round(double v) {
        return Math.round(v * 100d) / 100d;
    }
}
