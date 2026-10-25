package com.dronemission.notifications;

import com.dronemission.missions.Mission;
import com.dronemission.missions.MissionAssignment;
import com.dronemission.missions.event.MissionEvent;
import com.dronemission.missions.event.MissionEventListener;
import com.dronemission.users.RoleName;
import com.dronemission.users.User;
import com.dronemission.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Observador que avisa a las personas interesadas cuando una misión cambia. */
@Component
@RequiredArgsConstructor
public class NotificationMissionListener implements MissionEventListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    public void onMissionEvent(MissionEvent event) {
        Mission m = event.mission();
        String ref = m.getCode() + " (" + m.getName() + ")";
        User pilotUser = m.getActiveAssignment().map(MissionAssignment::getPilot).map(p -> p.getUser()).orElse(null);
        User clientUser = m.getClient().getUser();
        User creator = m.getCreatedBy();

        switch (event.type()) {
            case ASIGNADA -> {
                notificationService.notify(listOf(pilotUser), "Nueva misión asignada",
                        "Se te asignó la misión " + ref + " para el " + m.getScheduledDate() + " a las " + m.getStartTime() + ".", m);
                notificationService.notify(listOf(clientUser), "Misión programada",
                        "Tu misión " + ref + " quedó programada para el " + m.getScheduledDate() + ".", m);
            }
            case INICIADA -> notificationService.notify(listOf(clientUser, creator), "Misión en vuelo",
                    "La misión " + ref + " despegó.", m);
            case PAUSADA, REANUDADA -> notificationService.notify(listOf(creator), "Misión " + event.type().name().toLowerCase(),
                    "La misión " + ref + " fue " + event.type().name().toLowerCase() + ". " + nullToEmpty(event.detail()), m);
            case FINALIZADA -> notificationService.notify(listOf(clientUser, creator), "Misión finalizada",
                    "La misión " + ref + " finalizó. Ya puedes consultar el reporte.", m);
            case CANCELADA -> notificationService.notify(listOf(pilotUser, clientUser), "Misión cancelada",
                    "La misión " + ref + " fue cancelada. Motivo: " + nullToEmpty(event.detail()), m);
            case INCIDENTE_CRITICO -> {
                List<User> recipients = new ArrayList<>(userRepository.findByRoleNameAndActiveTrue(RoleName.OPERADOR));
                recipients.addAll(userRepository.findByRoleNameAndActiveTrue(RoleName.ADMINISTRADOR));
                recipients.add(creator);
                notificationService.notify(recipients, "Incidente crítico",
                        "Incidente crítico en la misión " + ref + ": " + nullToEmpty(event.detail()), m);
            }
            default -> {
                // Los demás eventos solo se auditan
            }
        }
    }

    private static List<User> listOf(User... users) {
        List<User> list = new ArrayList<>();
        for (User u : users) {
            if (u != null) list.add(u);
        }
        return list;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
