package es.oamonitor.controller;

import es.oamonitor.dto.ApcImportResult;
import es.oamonitor.dto.InstitutionDto;
import es.oamonitor.model.enums.OaiStatus;
import es.oamonitor.service.ApcImportService;
import es.oamonitor.service.InstitutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final InstitutionService institutionService;
    private final ApcImportService apcImportService;

    public AdminController(InstitutionService institutionService, ApcImportService apcImportService) {
        this.institutionService = institutionService;
        this.apcImportService = apcImportService;
    }

    @GetMapping("/institutions")
    public List<InstitutionDto> getAllInstitutions() {
        return institutionService.getAllForAdmin();
    }

    @PutMapping("/institutions/{id}/status")
    public ResponseEntity<InstitutionDto> updateStatus(
            @PathVariable UUID id,
            @RequestParam OaiStatus status) {
        return institutionService.updateOaiStatus(id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/institutions/{id}/activation")
    public ResponseEntity<InstitutionDto> updateActivationData(
            @PathVariable UUID id,
            @RequestParam(required = false) String rorId,
            @RequestParam(required = false) String oaiEndpoint,
            @RequestParam(required = false) String oaiMetadataPrefix) {
        return institutionService.updateActivationData(id, rorId, oaiEndpoint, oaiMetadataPrefix)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/apc/import")
    public ApcImportResult importApc() {
        return apcImportService.importFromOpenApc();
    }
}
