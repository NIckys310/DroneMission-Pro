package com.dronemission.notifications;

import com.dronemission.auth.CurrentUserService;
import com.dronemission.common.exception.ResourceNotFoundException;
import com.dronemission.missions.Mission;
import com.dronemission.notifications.dto.NotificationResponse;
import com.dronemission.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;

    /** Envía la misma notificación a varios usuarios (ignora nulos y repetidos). */
    @Transactional
    public void notify(Collection<User> recipients, String title, String message, Mission mission) {
        recipients.stream().filter(Objects::nonNull).distinct().forEach(user -> {
            Notification n = new Notification();
            n.setUser(user);
            n.setTitle(title);
            n.setMessage(message.length() > 500 ? message.substring(0, 500) : message);
            n.setMission(mission);
            notificationRepository.save(n);
        });
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findMine() {
        Long userId = currentUserService.get().id();
        return notificationRepository.findByUserIdOrderByDateDesc(userId, PageRequest.of(0, 30)).stream()
                .map(n -> new NotificationResponse(n.getId(), n.getTitle(), n.getMessage(),
                        n.getMission() != null ? n.getMission().getId() : null, n.isRead(), n.getDate()))
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        return notificationRepository.countByUserIdAndReadFalse(currentUserService.get().id());
    }

    @Transactional
    public void markAsRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", id));
        if (!n.getUser().getId().equals(currentUserService.get().id())) {
            throw new AccessDeniedException("La notificación no te pertenece");
        }
        n.setRead(true);
    }

    @Transactional
    public void markAllAsRead() {
        notificationRepository.findByUserIdAndReadFalse(currentUserService.get().id()).forEach(n -> n.setRead(true));
    }
}
