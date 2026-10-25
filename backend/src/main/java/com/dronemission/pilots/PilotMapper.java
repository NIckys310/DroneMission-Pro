package com.dronemission.pilots;

import com.dronemission.pilots.dto.CertificationDto;
import com.dronemission.pilots.dto.PilotRequest;
import com.dronemission.pilots.dto.PilotResponse;

import java.time.LocalDate;
import java.util.List;

/** Convierte entre la entidad Pilot y sus DTOs. */
public final class PilotMapper {

    private PilotMapper() {
    }

    public static void apply(PilotRequest req, Pilot pilot) {
        pilot.setFirstName(req.firstName().trim());
        pilot.setLastName(req.lastName().trim());
        pilot.setDocument(req.document().trim());
        pilot.setEmail(req.email().trim().toLowerCase());
        pilot.setPhone(req.phone().trim());
        pilot.setLicenseNumber(req.licenseNumber().trim().toUpperCase());
        pilot.setLicenseExpiresOn(req.licenseExpiresOn());
        if (req.flightHours() != null) {
            pilot.setFlightHours(req.flightHours());
        }
        List<CertificationDto> certs = req.certifications() == null ? List.of() : req.certifications();
        pilot.replaceCertifications(certs.stream().map(PilotMapper::toEntity).toList());
    }

    public static PilotCertification toEntity(CertificationDto dto) {
        PilotCertification c = new PilotCertification();
        c.setName(dto.name().trim());
        c.setIssuer(dto.issuer().trim());
        c.setIssuedOn(dto.issuedOn());
        c.setExpiresOn(dto.expiresOn());
        return c;
    }

    public static PilotResponse toResponse(Pilot p, long missionCount) {
        List<CertificationDto> certs = p.getCertifications().stream()
                .map(c -> new CertificationDto(c.getId(), c.getName(), c.getIssuer(), c.getIssuedOn(), c.getExpiresOn()))
                .toList();
        return new PilotResponse(p.getId(), p.getFirstName(), p.getLastName(), p.getFullName(), p.getDocument(),
                p.getEmail(), p.getPhone(), p.getLicenseNumber(), p.getLicenseExpiresOn(),
                p.hasValidLicenseOn(LocalDate.now()), p.getFlightHours(), p.getStatus().name(), p.isActive(),
                p.getUser() != null ? p.getUser().getEmail() : null, certs, missionCount);
    }
}
