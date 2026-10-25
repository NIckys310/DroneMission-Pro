package com.dronemission.evidence;

import com.dronemission.evidence.dto.EvidenceRequest;
import com.dronemission.evidence.dto.EvidenceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Evidencias")
public class EvidenceController {

    private final EvidenceService evidenceService;

    @GetMapping("/missions/{missionId}/evidences")
    @PreAuthorize("hasAuthority('MISIONES_VER')")
    public List<EvidenceResponse> byMission(@PathVariable Long missionId) {
        return evidenceService.findByMission(missionId);
    }

    @PostMapping(value = "/missions/{missionId}/evidences", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('EVIDENCIAS_REGISTRAR')")
    @Operation(summary = "Registrar evidencia subiendo un archivo (multipart/form-data)")
    public EvidenceResponse upload(@PathVariable Long missionId,
                                   @Valid @ModelAttribute EvidenceRequest request,
                                   @RequestPart(value = "file", required = false) MultipartFile file) {
        return evidenceService.create(missionId, request, file);
    }

    @PostMapping(value = "/missions/{missionId}/evidences", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('EVIDENCIAS_REGISTRAR')")
    @Operation(summary = "Registrar evidencia con una URL o como observación (JSON)")
    public EvidenceResponse createWithUrl(@PathVariable Long missionId, @Valid @RequestBody EvidenceRequest request) {
        return evidenceService.create(missionId, request, null);
    }

    @GetMapping("/evidences/{id}/file")
    @PreAuthorize("hasAuthority('MISIONES_VER')")
    @Operation(summary = "Descargar el archivo de una evidencia")
    public ResponseEntity<Resource> file(@PathVariable Long id) {
        EvidenceService.StoredFile stored = evidenceService.loadFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(stored.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + stored.fileName() + "\"")
                .body(stored.resource());
    }

    @DeleteMapping("/evidences/{id}")
    @PreAuthorize("hasAuthority('MISIONES_GESTIONAR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        evidenceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
