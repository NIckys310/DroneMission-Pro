package com.dronemission.reports;

import com.dronemission.reports.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reportes")
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    @PreAuthorize("hasAuthority('REPORTES_VER')")
    @Operation(summary = "Historial de reportes generados")
    public List<ReportResponse> history() {
        return reportService.history();
    }

    @GetMapping("/missions/{missionId}")
    @PreAuthorize("hasAuthority('REPORTES_VER')")
    public MissionReport missionReport(@PathVariable Long missionId) {
        return reportService.missionReport(missionId);
    }

    @GetMapping("/missions/{missionId}/pdf")
    @PreAuthorize("hasAuthority('REPORTES_VER')")
    @Operation(summary = "Descargar el reporte de misión en PDF")
    public ResponseEntity<byte[]> missionReportPdf(@PathVariable Long missionId) {
        return pdf(reportService.missionPdf(missionId));
    }

    @GetMapping("/drones")
    @PreAuthorize("hasAuthority('REPORTES_GENERAR')")
    public DroneReport droneReport() {
        return reportService.droneReport();
    }

    @GetMapping("/drones/pdf")
    @PreAuthorize("hasAuthority('REPORTES_GENERAR')")
    public ResponseEntity<byte[]> droneReportPdf() {
        return pdf(reportService.dronePdf());
    }

    @GetMapping("/pilots")
    @PreAuthorize("hasAuthority('REPORTES_GENERAR')")
    public PilotReport pilotReport() {
        return reportService.pilotReport();
    }

    @GetMapping("/pilots/pdf")
    @PreAuthorize("hasAuthority('REPORTES_GENERAR')")
    public ResponseEntity<byte[]> pilotReportPdf() {
        return pdf(reportService.pilotPdf());
    }

    @GetMapping("/maintenance")
    @PreAuthorize("hasAuthority('REPORTES_GENERAR')")
    public MaintenanceReport maintenanceReport() {
        return reportService.maintenanceReport();
    }

    @GetMapping("/maintenance/pdf")
    @PreAuthorize("hasAuthority('REPORTES_GENERAR')")
    public ResponseEntity<byte[]> maintenanceReportPdf() {
        return pdf(reportService.maintenancePdf());
    }

    private ResponseEntity<byte[]> pdf(ReportService.PdfFile file) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .body(file.content());
    }
}
