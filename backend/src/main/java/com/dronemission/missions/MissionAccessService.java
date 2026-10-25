package com.dronemission.missions;

import com.dronemission.auth.AuthenticatedUser;
import com.dronemission.auth.CurrentUserService;
import com.dronemission.users.RoleName;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Reglas de acceso a las misiones según el rol:
 *  - El cliente solo ve sus propias misiones (regla 12).
 *  - El piloto solo ve y ejecuta las misiones que tiene asignadas.
 *  - Administrador y operador ven todas.
 * También calcula qué acciones puede hacer el usuario sobre una misión.
 */
@Service
@RequiredArgsConstructor
public class MissionAccessService {

    private final CurrentUserService currentUserService;

    public boolean canView(Mission mission, AuthenticatedUser user) {
        if (user.hasRole(RoleName.PILOTO)) {
            return isAssignedPilot(mission, user);
        }
        if (user.hasRole(RoleName.CLIENTE)) {
            return mission.getClient().getUser() != null && mission.getClient().getUser().getId().equals(user.id());
        }
        return user.permissions().contains("MISIONES_VER");
    }

    public void checkView(Mission mission) {
        if (!canView(mission, currentUserService.get())) {
            throw new AccessDeniedException("No tienes acceso a la misión " + mission.getCode());
        }
    }

    public boolean isAssignedPilot(Mission mission, AuthenticatedUser user) {
        return mission.getActiveAssignment()
                .map(a -> a.getPilot().getUser() != null && a.getPilot().getUser().getId().equals(user.id()))
                .orElse(false);
    }

    /** Preparar, despegar, pausar, reanudar y finalizar: solo el piloto asignado (o el administrador). */
    public void checkExecute(Mission mission) {
        AuthenticatedUser user = currentUserService.get();
        if (!canExecute(mission, user)) {
            throw new AccessDeniedException("Solo el piloto asignado puede operar la misión " + mission.getCode());
        }
    }

    public boolean canExecute(Mission mission, AuthenticatedUser user) {
        return user.permissions().contains("MISIONES_EJECUTAR") && (user.isAdmin() || isAssignedPilot(mission, user));
    }

    public boolean canManage(AuthenticatedUser user) {
        return user.permissions().contains("MISIONES_GESTIONAR");
    }

    /** Incidentes y evidencias: personal de operaciones o el piloto asignado. */
    public boolean canContribute(Mission mission, AuthenticatedUser user, String permission) {
        return user.permissions().contains(permission) && (canManage(user) || user.isAdmin() || isAssignedPilot(mission, user));
    }

    public List<String> availableActions(Mission mission) {
        AuthenticatedUser user = currentUserService.get();
        MissionStatus s = mission.getStatus();
        boolean manage = canManage(user);
        boolean execute = canExecute(mission, user);
        List<MissionAction> actions = new ArrayList<>();

        if (manage && s.isEditable()) actions.add(MissionAction.EDIT);
        if (manage && s == MissionStatus.BORRADOR) actions.add(MissionAction.DELETE);
        if (manage && s == MissionStatus.BORRADOR) actions.add(MissionAction.SUBMIT);
        if (manage && (s == MissionStatus.PENDIENTE || s == MissionStatus.PROGRAMADA)) actions.add(MissionAction.ASSIGN);
        if (manage && s.canTransitionTo(MissionStatus.CANCELADA)) actions.add(MissionAction.CANCEL);
        if (execute && s == MissionStatus.PROGRAMADA) actions.add(MissionAction.PREPARE);
        if (execute && s == MissionStatus.EN_PREPARACION) actions.add(MissionAction.START);
        if (execute && s == MissionStatus.EN_VUELO) actions.add(MissionAction.PAUSE);
        if (execute && s == MissionStatus.PAUSADA) actions.add(MissionAction.RESUME);
        if (execute && (s == MissionStatus.EN_VUELO || s == MissionStatus.PAUSADA)) actions.add(MissionAction.FINISH);
        if ((execute || manage) && s != MissionStatus.CANCELADA) actions.add(MissionAction.OBSERVE);
        if (s != MissionStatus.BORRADOR && s != MissionStatus.CANCELADA && canContribute(mission, user, "INCIDENTES_REGISTRAR")) actions.add(MissionAction.INCIDENT);
        if (evidenceAllowed(s) && canContribute(mission, user, "EVIDENCIAS_REGISTRAR")) actions.add(MissionAction.EVIDENCE);
        if (s == MissionStatus.FINALIZADA && user.permissions().contains("REPORTES_VER")) actions.add(MissionAction.REPORT);

        return actions.stream().map(Enum::name).toList();
    }

    public static boolean evidenceAllowed(MissionStatus status) {
        return EnumSet.of(MissionStatus.EN_PREPARACION, MissionStatus.EN_VUELO, MissionStatus.PAUSADA,
                MissionStatus.FINALIZADA).contains(status);
    }
}
