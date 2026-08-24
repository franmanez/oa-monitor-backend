package es.oamonitor.controller;

import es.oamonitor.dto.*;
import es.oamonitor.service.InstitutionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/institutions")
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @GetMapping
    public InstitutionPageResponse getAll(Pageable pageable) {
        return institutionService.getAllInstitutions(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstitutionDto> getById(@PathVariable UUID id) {
        return institutionService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<InstitutionSummaryDto> getSummary(@PathVariable UUID id, @RequestParam(required = false) Integer year) {
        return institutionService.getSummary(id, year)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/oa-breakdown")
    public ResponseEntity<OaBreakdownDto> getOaBreakdown(@PathVariable UUID id) {
        return institutionService.getOaBreakdown(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/apc-estimate")
    public ResponseEntity<ApcEstimateDto> getApcEstimate(@PathVariable UUID id) {
        return institutionService.getApcEstimate(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/apc/calculate")
    public ResponseEntity<ApcEstimateDto> calculateApc(@PathVariable UUID id) {
        return institutionService.calculateAndPersistApc(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/publications")
    public Page<PublicationDto> getPublications(
            @PathVariable UUID id,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return institutionService.getPublicationsFiltered(id, year, status, search, pageable);
    }

    @GetMapping("/ranking")
    public List<InstitutionRankingDto> getRanking(@RequestParam(required = false) Integer year) {
        return institutionService.getRanking(year);
    }

    @GetMapping("/ranking/years")
    public List<Integer> getAvailableRankingYears() {
        return institutionService.getAvailableRankingYears();
    }

    @GetMapping("/stats/national")
    public NationalStatsDto getNationalStats() {
        return institutionService.getNationalStats();
    }

    @GetMapping("/stats/scatter")
    public List<InstitutionScatterDto> getScatter(@RequestParam int year) {
        return institutionService.getInstitutionScatter(year);
    }

    @GetMapping("/{id}/journals/apc")
    public List<JournalApcDto> getJournalsByApc(@PathVariable UUID id, @RequestParam(required = false) Integer year) {
        return institutionService.getJournalsByApc(id, year);
    }

    @GetMapping("/{id}/journals/top")
    public List<JournalCountDto> getTopJournals(@PathVariable UUID id, @RequestParam(required = false) Integer year, @RequestParam(defaultValue = "10") int limit) {
        return institutionService.getTopJournals(id, year, limit);
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<org.springframework.core.io.Resource> exportCsv(
            @PathVariable UUID id,
            @RequestParam(required = false) Integer year) {
        var pubs = institutionService.getPublicationsForExport(id, year);
        var sb = new StringBuilder();
        sb.append("DOI,Título,Año,Revista,ISSN,Estado OA OpenAlex,Estado OA Resuelto,URL repositorio,Fuente,APC estimado €\n");
        for (var pub : pubs) {
            sb.append(escapeCsv(pub.getDoi())).append(',');
            sb.append(escapeCsv(pub.getTitle())).append(',');
            sb.append(pub.getPublicationYear() != null ? pub.getPublicationYear() : "").append(',');
            sb.append(escapeCsv(pub.getJournalName())).append(',');
            sb.append(escapeCsv(pub.getJournalIssn())).append(',');
            sb.append(pub.getOaStatusOpenalex() != null ? pub.getOaStatusOpenalex().name() : "").append(',');
            sb.append(pub.getOaStatusResolved() != null ? pub.getOaStatusResolved().name() : "").append(',');
            sb.append(escapeCsv(pub.getRepositoryUrl())).append(',');
            sb.append(pub.getSource() != null ? pub.getSource().name() : "").append(',');
            sb.append(pub.getApcEstimatedEur() != null ? pub.getApcEstimatedEur().toString() : "").append('\n');
        }

        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        var resource = new org.springframework.core.io.ByteArrayResource(bytes);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=publicaciones.csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(resource);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @PostMapping
    public ResponseEntity<InstitutionDto> create(@Valid @RequestBody CreateInstitutionRequest request) {
        try {
            InstitutionDto created = institutionService.createInstitution(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}/oai-config")
    public ResponseEntity<InstitutionDto> updateOaiConfig(
            @PathVariable UUID id,
            @RequestParam String oaiEndpoint,
            @RequestParam String oaiMetadataPrefix) {
        return institutionService.updateOaiConfig(id, oaiEndpoint, oaiMetadataPrefix)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
