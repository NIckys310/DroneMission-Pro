package com.dronemission.reports;

import com.dronemission.reports.dto.DroneReport;
import com.dronemission.reports.dto.MaintenanceReport;
import com.dronemission.reports.dto.MissionReport;
import com.dronemission.reports.dto.PilotReport;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Convierte los datos de los reportes en documentos PDF (librería OpenPDF). */
@Component
public class PdfReportGenerator {

    private static final Color PRIMARY = new Color(14, 75, 128);
    private static final Color LIGHT = new Color(232, 240, 248);
    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PRIMARY);
    private static final Font SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font NOTICE = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, new Color(160, 80, 0));
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    public byte[] missionReport(MissionReport r) {
        return build("Reporte de misión " + r.code(), doc -> {
            section(doc, "Información de la misión");
            keyValues(doc,
                    "Código", r.code(), "Nombre", r.name(),
                    "Tipo", r.type(), "Estado", r.status(),
                    "Prioridad", r.priority(), "Ubicación", r.location(),
                    "Coordenadas", r.latitude() + ", " + r.longitude(), "Fecha programada", String.valueOf(r.scheduledDate()),
                    "Horario", r.startTime() + " - " + r.estimatedEndTime(), "Duración real", r.durationMinutes() == null ? "-" : r.durationMinutes() + " min",
                    "Inicio real", fmt(r.actualStart()), "Fin real", fmt(r.actualEnd()),
                    "Objetivo", r.objective(), "Patrón de vuelo", r.flightStrategy() + " (" + r.waypoints() + " puntos)");

            section(doc, "Cliente");
            keyValues(doc, "Nombre", r.client().name(), "Documento", r.client().document(),
                    "Correo", r.client().email(), "Teléfono", r.client().phone());

            section(doc, "Recursos asignados");
            keyValues(doc,
                    "Drone", r.drone() == null ? "Sin asignar" : r.drone().code() + " - " + r.drone().name(),
                    "Modelo", r.drone() == null ? "-" : r.drone().manufacturer() + " " + r.drone().model(),
                    "Piloto", r.pilot() == null ? "Sin asignar" : r.pilot().fullName(),
                    "Licencia", r.pilot() == null ? "-" : r.pilot().licenseNumber());

            section(doc, "Telemetría resumida");
            doc.add(new Paragraph(r.telemetryNotice(), NOTICE));
            var t = r.telemetry();
            keyValues(doc, "Lecturas", String.valueOf(t.readings()), "Distancia", Math.round(t.distanceM()) + " m",
                    "Altitud máxima", t.maxAltitudeM() + " m", "Velocidad promedio", t.avgSpeedKmh() + " km/h",
                    "Tiempo de vuelo", (t.flightTimeS() / 60) + " min " + (t.flightTimeS() % 60) + " s",
                    "Batería", t.startBattery() + " % a " + t.endBattery() + " % (consumo " + t.batteryUsed() + " %)");

            section(doc, "Incidentes (" + r.incidents().size() + ")");
            PdfPTable inc = table(new float[]{2.2f, 2.2f, 1.2f, 5f, 1.6f}, "Fecha", "Tipo", "Severidad", "Descripción", "Estado");
            r.incidents().forEach(i -> row(inc, fmt(i.date()), i.type(), i.severity(), i.description(), i.status()));
            emptyRow(inc, r.incidents().isEmpty(), 5);
            doc.add(inc);

            section(doc, "Evidencias (" + r.evidences().size() + ")");
            PdfPTable ev = table(new float[]{2f, 3f, 5f, 2.2f}, "Tipo", "Nombre", "Descripción", "Fecha");
            r.evidences().forEach(e -> row(ev, e.type(), e.name(), e.description() == null ? "" : e.description(), fmt(e.date())));
            emptyRow(ev, r.evidences().isEmpty(), 4);
            doc.add(ev);

            if (r.observations() != null && !r.observations().isBlank()) {
                section(doc, "Observaciones");
                doc.add(new Paragraph(r.observations(), NORMAL));
            }
        });
    }

    public byte[] droneReport(DroneReport r) {
        return build("Reporte de drones", doc -> {
            section(doc, "Resumen de la flota");
            keyValues(doc, "Total de drones", String.valueOf(r.total()), "Disponibles", String.valueOf(r.available()),
                    "En misión", String.valueOf(r.inMission()), "En mantenimiento", String.valueOf(r.inMaintenance()),
                    "Fuera de servicio", String.valueOf(r.outOfService()), "Horas de vuelo totales", r.totalFlightHours() + " h");
            section(doc, "Detalle");
            PdfPTable t = table(new float[]{1.4f, 2.4f, 3f, 2f, 2.4f, 1.2f, 1.4f, 2f},
                    "Código", "Nombre", "Modelo", "Tipo", "Estado", "Batería", "Horas", "Próx. mant.");
            r.drones().forEach(d -> row(t, d.code(), d.name(), d.model(), d.type(), d.status(), d.batteryLevel() + " %",
                    String.valueOf(d.flightHours()), String.valueOf(d.nextMaintenanceOn())));
            doc.add(t);
        });
    }

    public byte[] pilotReport(PilotReport r) {
        return build("Reporte de pilotos", doc -> {
            section(doc, "Resumen");
            keyValues(doc, "Pilotos activos", String.valueOf(r.total()), "Disponibles", String.valueOf(r.available()),
                    "Licencias vencidas", String.valueOf(r.expiredLicenses()), "Horas de vuelo totales", r.totalFlightHours() + " h");
            section(doc, "Detalle");
            PdfPTable t = table(new float[]{3f, 3f, 2f, 1.6f, 2.4f, 1.3f, 1.3f, 1.4f},
                    "Piloto", "Licencia", "Vence", "Vigente", "Estado", "Horas", "Misiones", "Finalizadas");
            r.pilots().forEach(p -> row(t, p.fullName(), p.licenseNumber(), String.valueOf(p.licenseExpiresOn()),
                    p.licenseValid() ? "Sí" : "NO", p.status(), String.valueOf(p.flightHours()),
                    String.valueOf(p.missions()), String.valueOf(p.finishedMissions())));
            doc.add(t);
        });
    }

    public byte[] maintenanceReport(MaintenanceReport r) {
        return build("Reporte de mantenimiento", doc -> {
            section(doc, "Resumen");
            keyValues(doc, "Total", String.valueOf(r.total()), "Programados", String.valueOf(r.scheduled()),
                    "En proceso", String.valueOf(r.inProgress()), "Completados", String.valueOf(r.completed()),
                    "Cancelados", String.valueOf(r.cancelled()), "Costo total", money(r.totalCost()));
            section(doc, "Costo por drone");
            PdfPTable c = table(new float[]{2f, 4f, 2f, 3f}, "Drone", "Nombre", "Mantenimientos", "Costo");
            r.costByDrone().forEach(x -> row(c, x.droneCode(), x.droneName(), String.valueOf(x.count()), money(x.cost())));
            doc.add(c);
            section(doc, "Historial");
            PdfPTable h = table(new float[]{1.8f, 1.6f, 2f, 2f, 2.6f, 2f, 4f},
                    "Fecha", "Drone", "Tipo", "Estado", "Técnico", "Costo", "Descripción");
            r.history().forEach(m -> row(h, String.valueOf(m.date()), m.droneCode(), m.type(), m.status(), m.technician(),
                    money(m.cost()), m.description()));
            doc.add(h);
        });
    }

    // ------------------------------------------------------------------ utilidades

    @FunctionalInterface
    private interface Content {
        void write(Document doc) throws DocumentException;
    }

    private byte[] build(String title, Content content) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
        PdfWriter.getInstance(doc, out);
        doc.open();
        try {
            Paragraph brand = new Paragraph("DroneMission Pro", TITLE);
            doc.add(brand);
            doc.add(new Paragraph(title, SUBTITLE));
            doc.add(new Paragraph("Generado el " + LocalDateTime.now().format(DATE_TIME), NORMAL));
            doc.add(Chunk.NEWLINE);
            content.write(doc);
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    private void section(Document doc, String text) throws DocumentException {
        Paragraph p = new Paragraph(text, SUBTITLE);
        p.setSpacingBefore(10);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    /** Tabla de 4 columnas con pares etiqueta / valor. */
    private void keyValues(Document doc, String... pairs) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{2f, 3f, 2f, 3f});
        t.setWidthPercentage(100);
        for (int i = 0; i < pairs.length; i += 2) {
            PdfPCell key = new PdfPCell(new Phrase(pairs[i], BOLD));
            key.setBackgroundColor(LIGHT);
            key.setPadding(4);
            t.addCell(key);
            PdfPCell value = new PdfPCell(new Phrase(pairs[i + 1] == null ? "-" : pairs[i + 1], NORMAL));
            value.setPadding(4);
            t.addCell(value);
        }
        if ((pairs.length / 2) % 2 != 0) {
            t.addCell(new PdfPCell(new Phrase("")));
            t.addCell(new PdfPCell(new Phrase("")));
        }
        doc.add(t);
    }

    private PdfPTable table(float[] widths, String... headers) {
        PdfPTable t = new PdfPTable(widths);
        t.setWidthPercentage(100);
        t.setHeaderRows(1);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEADER));
            cell.setBackgroundColor(PRIMARY);
            cell.setPadding(4);
            t.addCell(cell);
        }
        return t;
    }

    private void row(PdfPTable t, String... values) {
        for (String v : values) {
            PdfPCell cell = new PdfPCell(new Phrase(v == null ? "-" : v, NORMAL));
            cell.setPadding(3);
            t.addCell(cell);
        }
    }

    private void emptyRow(PdfPTable t, boolean empty, int columns) {
        if (empty) {
            PdfPCell cell = new PdfPCell(new Phrase("Sin registros", NORMAL));
            cell.setColspan(columns);
            cell.setPadding(4);
            t.addCell(cell);
        }
    }

    private static String fmt(LocalDateTime dt) {
        return dt == null ? "-" : dt.format(DATE_TIME);
    }

    private static String money(BigDecimal value) {
        return MONEY.format(value);
    }
}
