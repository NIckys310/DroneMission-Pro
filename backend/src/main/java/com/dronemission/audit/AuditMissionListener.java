package com.dronemission.audit;

import com.dronemission.missions.event.MissionEvent;
import com.dronemission.missions.event.MissionEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Observador que deja en la bitácora de auditoría cada evento de una misión. */
@Component
@RequiredArgsConstructor
public class AuditMissionListener implements MissionEventListener {

    private final AuditService auditService;

    @Override
    public void onMissionEvent(MissionEvent event) {
        String action = switch (event.type()) {
            case CREADA -> "CREAR";
            case ACTUALIZADA -> "ACTUALIZAR";
            case ENVIADA -> "ENVIAR";
            case ASIGNADA -> "ASIGNAR";
            case PREPARADA -> "PREPARAR";
            case INICIADA -> "INICIAR";
            case PAUSADA -> "PAUSAR";
            case REANUDADA -> "REANUDAR";
            case FINALIZADA -> "FINALIZAR";
            case CANCELADA -> "CANCELAR";
            case ELIMINADA -> "ELIMINAR";
            case INCIDENTE_CRITICO -> "INCIDENTE_CRITICO";
        };
        String description = "Misión " + event.mission().getCode()
                + (event.detail() != null && !event.detail().isBlank() ? ": " + event.detail() : "");
        auditService.log(action, "MISION", event.mission().getId(), description);
    }
}
