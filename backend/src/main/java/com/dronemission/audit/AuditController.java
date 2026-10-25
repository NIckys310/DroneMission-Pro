package com.dronemission.audit;

import com.dronemission.audit.dto.AuditResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Auditoría")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDITORIA_VER')")
    @Operation(summary = "Últimos registros de auditoría")
    public List<AuditResponse> recent(@RequestParam(defaultValue = "200") int limit) {
        return auditService.findRecent(Math.min(limit, 1000));
    }
}
