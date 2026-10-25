package com.dronemission.users;

import com.dronemission.users.dto.ApproveUserRequest;
import com.dronemission.users.dto.UserRequest;
import com.dronemission.users.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('USUARIOS_GESTIONAR')")
@Tag(name = "Usuarios")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.update(id, request);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Aprobar una cuenta registrada y asignarle su rol")
    public UserResponse approve(@PathVariable Long id, @Valid @RequestBody ApproveUserRequest request) {
        return userService.approve(id, request);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Rechazar (eliminar) una cuenta pendiente")
    public ResponseEntity<Void> reject(@PathVariable Long id) {
        userService.reject(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar usuario")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
