package com.dronemission.reports;

import com.dronemission.missions.Mission;
import com.dronemission.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Historial de reportes generados. El PDF no se guarda: se regenera a partir
 * de los datos actuales cada vez que se descarga (evita duplicar información).
 */
@Entity
@Table(name = "reportes")
@Getter
@Setter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private ReportType type;

    @Column(name = "titulo", nullable = false, length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mision_id")
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generado_por")
    private User generatedBy;

    @Column(name = "generado_en", nullable = false)
    private LocalDateTime generatedAt = LocalDateTime.now();

    @Column(name = "resumen", nullable = false, length = 2000)
    private String summary;
}
