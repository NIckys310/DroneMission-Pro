package com.dronemission.users;

import com.dronemission.users.dto.PermissionResponse;
import com.dronemission.users.dto.RolePermissionsRequest;
import com.dronemission.users.dto.RoleResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Roles y permisos")
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/roles")
    @PreAuthorize("hasAnyAuthority('USUARIOS_GESTIONAR', 'ROLES_GESTIONAR')")
    public List<RoleResponse> roles() {
        return roleService.findAll();
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLES_GESTIONAR')")
    public List<PermissionResponse> permissions() {
        return roleService.findAllPermissions();
    }

    @PutMapping("/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLES_GESTIONAR')")
    public RoleResponse updatePermissions(@PathVariable Long id, @Valid @RequestBody RolePermissionsRequest request) {
        return roleService.updatePermissions(id, request.permissions());
    }
}
