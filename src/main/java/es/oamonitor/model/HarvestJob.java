package es.oamonitor.model;

import es.oamonitor.model.enums.HarvestStatus;
import es.oamonitor.model.enums.HarvestType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "harvest_job")
public class HarvestJob {

    @Id
    @Convert(converter = BinaryUuidConverter.class)
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Enumerated(EnumType.STRING)
    @Column(name = "harvest_type", nullable = false)
    private HarvestType harvestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HarvestStatus status;

    @Column(name = "records_fetched")
    private Integer recordsFetched = 0;

    @Column(name = "records_updated")
    private Integer recordsUpdated = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @PrePersist
    void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public HarvestType getHarvestType() { return harvestType; }
    public void setHarvestType(HarvestType harvestType) { this.harvestType = harvestType; }

    public HarvestStatus getStatus() { return status; }
    public void setStatus(HarvestStatus status) { this.status = status; }

    public Integer getRecordsFetched() { return recordsFetched; }
    public void setRecordsFetched(Integer recordsFetched) { this.recordsFetched = recordsFetched; }

    public Integer getRecordsUpdated() { return recordsUpdated; }
    public void setRecordsUpdated(Integer recordsUpdated) { this.recordsUpdated = recordsUpdated; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
}
