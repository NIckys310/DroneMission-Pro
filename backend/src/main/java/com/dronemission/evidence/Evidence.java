package com.dronemission.evidence;

import com.dronemission.missions.Mission;
import com.dronemission.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Evidencia de una misión. "url" puede ser:
 *  - una URL externa o ruta pública (/demo/...),
 *  - "file:<nombre>" para archivos subidos al almacenamiento local.
 */
@Entity
@Table(name = "evidencias")
@Getter
@Setter
@NoArgsConstructor
public class Evidence {

    public static final String FILE_PREFIX = "file:";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mision_id")
    private Mission mission;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private EvidenceType type;

    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "descripcion", length = 1000)
    private String description;

    @Column(name = "visible_cliente", nullable = false)
    private boolean visibleToClient = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subido_por")
    private User uploadedBy;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    public boolean isStoredFile() {
        return url != null && url.startsWith(FILE_PREFIX);
    }
}
