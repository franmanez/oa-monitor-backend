package es.oamonitor.controller;

import es.oamonitor.dto.HarvestJobDto;
import es.oamonitor.model.HarvestJob;
import es.oamonitor.repository.HarvestJobRepository;
import es.oamonitor.service.OpenAlexHarvestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/harvest")
public class HarvestController {

    private final OpenAlexHarvestService openAlexHarvestService;
    private final HarvestJobRepository harvestJobRepository;

    public HarvestController(OpenAlexHarvestService openAlexHarvestService,
                             HarvestJobRepository harvestJobRepository) {
        this.openAlexHarvestService = openAlexHarvestService;
        this.harvestJobRepository = harvestJobRepository;
    }

    @PostMapping("/trigger/{id}")
    public ResponseEntity<HarvestJobDto> triggerHarvest(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "2024") int year) {
        try {
            HarvestJob job = openAlexHarvestService.harvestInstitution(id, year);
            return ResponseEntity.ok(toDto(job));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/jobs")
    public Page<HarvestJobDto> getAllJobs(Pageable pageable) {
        return harvestJobRepository.findAll(pageable).map(this::toDto);
    }

    @GetMapping("/jobs/{institutionId}")
    public Page<HarvestJobDto> getJobsByInstitution(
            @PathVariable UUID institutionId,
            Pageable pageable) {
        return harvestJobRepository.findByInstitutionIdOrderByStartedAtDesc(institutionId, pageable)
                .map(this::toDto);
    }

    private HarvestJobDto toDto(HarvestJob job) {
        return new HarvestJobDto(
                job.getId().toString(),
                job.getInstitution() != null ? job.getInstitution().getName() : null,
                job.getHarvestType(),
                job.getStatus(),
                job.getRecordsFetched() != null ? job.getRecordsFetched() : 0,
                job.getRecordsUpdated() != null ? job.getRecordsUpdated() : 0,
                job.getErrorMessage(),
                job.getStartedAt(),
                job.getFinishedAt()
        );
    }
}
