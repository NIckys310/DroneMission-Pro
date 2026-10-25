package com.dronemission.clients;

import com.dronemission.audit.AuditService;
import com.dronemission.clients.dto.ClientRequest;
import com.dronemission.clients.dto.ClientResponse;
import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.common.exception.DuplicateResourceException;
import com.dronemission.common.exception.ResourceNotFoundException;
import com.dronemission.missions.MissionRepository;
import com.dronemission.missions.MissionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final MissionRepository missionRepository;
    private final AuditService auditService;

    /** Lista clientes filtrando por texto (nombre, documento o ciudad). */
    @Transactional(readOnly = true)
    public List<ClientResponse> findAll(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return clientRepository.findAllByOrderByNameAsc().stream()
                .filter(c -> q.isEmpty() || c.getName().toLowerCase().contains(q)
                        || c.getDocument().toLowerCase().contains(q) || c.getCity().toLowerCase().contains(q))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        return toResponse(get(id));
    }

    @Transactional
    public ClientResponse create(ClientRequest req) {
        if (clientRepository.existsByDocument(req.document().trim())) {
            throw new DuplicateResourceException("Ya existe un cliente con el documento " + req.document());
        }
        Client client = new Client();
        ClientMapper.apply(req, client);
        clientRepository.save(client);
        auditService.log("CREAR", "CLIENTE", client.getId(), "Cliente " + client.getName() + " creado");
        return toResponse(client);
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest req) {
        Client client = get(id);
        if (clientRepository.existsByDocumentAndIdNot(req.document().trim(), id)) {
            throw new DuplicateResourceException("Ya existe un cliente con el documento " + req.document());
        }
        ClientMapper.apply(req, client);
        auditService.log("ACTUALIZAR", "CLIENTE", id, "Cliente " + client.getName() + " actualizado");
        return toResponse(client);
    }

    /** Desactiva el cliente si no tiene misiones en curso o programadas. */
    @Transactional
    public void deactivate(Long id) {
        Client client = get(id);
        EnumSet<MissionStatus> open = EnumSet.of(MissionStatus.PENDIENTE, MissionStatus.PROGRAMADA,
                MissionStatus.EN_PREPARACION, MissionStatus.EN_VUELO, MissionStatus.PAUSADA);
        if (missionRepository.existsByClientIdAndStatusIn(id, open)) {
            throw new BusinessRuleException("El cliente tiene misiones abiertas; cancélalas o finalízalas antes de desactivarlo");
        }
        client.setActive(false);
        auditService.log("DESACTIVAR", "CLIENTE", id, "Cliente " + client.getName() + " desactivado");
    }

    public Client get(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private ClientResponse toResponse(Client c) {
        return ClientMapper.toResponse(c, missionRepository.countByClientId(c.getId()));
    }
}
