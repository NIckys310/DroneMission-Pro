package com.dronemission.audit;

import com.dronemission.audit.dto.AuditResponse;
import com.dronemission.auth.AuthenticatedUser;
import com.dronemission.auth.CurrentUserService;
import com.dronemission.users.User;
import com.dronemission.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Registra y consulta la bitácora de auditoría (regla de negocio 11). */
@Service
@RequiredArgsConstructor
public class AuditService {

    public static final String SYSTEM_USER = "sistema";

    private final AuditRepository auditRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    /** Registra una acción hecha por el usuario de la petición actual (o por el sistema). */
    @Transactional
    public void log(String action, String entity, Long entityId, String description) {
        Optional<AuthenticatedUser> current = currentUserService.find();
        User user = current.flatMap(u -> userRepository.findById(u.id())).orElse(null);
        save(user, current.map(AuthenticatedUser::email).orElse(SYSTEM_USER), action, entity, entityId, description);
    }

    /** Registra una acción indicando explícitamente el usuario (por ejemplo, en el login). */
    @Transactional
    public void logAs(User user, String email, String action, String entity, Long entityId, String description) {
        save(user, email, action, entity, entityId, description);
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> findRecent(int limit) {
        return auditRepository.findAllByOrderByDateDesc(PageRequest.of(0, limit)).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> findByEntity(String entity, Long entityId) {
        return auditRepository.findByEntityAndEntityIdOrderByDateAsc(entity, entityId).stream().map(this::toResponse).toList();
    }

    private void save(User user, String email, String action, String entity, Long entityId, String description) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setUserEmail(email);
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setDescription(description);
        auditRepository.save(log);
    }

    private AuditResponse toResponse(AuditLog log) {
        return new AuditResponse(log.getId(), log.getUserEmail(), log.getAction(), log.getEntity(),
                log.getEntityId(), log.getDescription(), log.getDate());
    }
}
