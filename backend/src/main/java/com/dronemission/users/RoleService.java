package com.dronemission.users;

import com.dronemission.audit.AuditService;
import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.common.exception.ResourceNotFoundException;
import com.dronemission.users.dto.PermissionResponse;
import com.dronemission.users.dto.RoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Consulta de roles y edición de los permisos de cada rol. */
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> findAllPermissions() {
        return permissionRepository.findAllByOrderByIdAsc().stream()
                .map(p -> new PermissionResponse(p.getId(), p.getCode(), p.getDescription())).toList();
    }

    @Transactional
    public RoleResponse updatePermissions(Long roleId, List<String> codes) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Rol", roleId));
        if (role.getName() == RoleName.ADMINISTRADOR) {
            throw new BusinessRuleException("Los permisos del administrador no se pueden modificar");
        }
        Map<String, Permission> byCode = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(Permission::getCode, Function.identity()));
        Set<Permission> newPermissions = new HashSet<>();
        for (String code : codes) {
            Permission p = byCode.get(code);
            if (p == null) {
                throw new BusinessRuleException("El permiso " + code + " no existe");
            }
            newPermissions.add(p);
        }
        role.setPermissions(newPermissions);
        auditService.log("ACTUALIZAR", "ROL", roleId, "Permisos del rol " + role.getName() + ": " + String.join(", ", codes));
        return toResponse(role);
    }

    private RoleResponse toResponse(Role r) {
        return new RoleResponse(r.getId(), r.getName().name(), r.getDescription(),
                r.getPermissions().stream().map(Permission::getCode).sorted().toList());
    }
}
