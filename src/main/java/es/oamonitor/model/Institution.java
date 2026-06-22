package es.oamonitor.model;

import es.oamonitor.model.enums.OaiStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "institution")
public class Institution {

    @Id
    @Convert(converter = BinaryUuidConverter.class)
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "ror_id", unique = true, nullable = false)
    private String rorId;

    @Column(name = "oai_endpoint")
    private String oaiEndpoint;

    @Column(name = "oai_metadata_prefix")
    private String oaiMetadataPrefix;

    @Column(name = "rebiun_code")
    private String rebiunCode;

    @Column(name = "last_harvest_at")
    private LocalDateTime lastHarvestAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "oai_status")
    private OaiStatus oaiStatus;

    @OneToMany(mappedBy = "institution", fetch = FetchType.LAZY)
    private List<Publication> publications = new ArrayList<>();

    @OneToMany(mappedBy = "institution", fetch = FetchType.LAZY)
    private List<HarvestJob> harvestJobs = new ArrayList<>();

    @PrePersist
    void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRorId() { return rorId; }
    public void setRorId(String rorId) { this.rorId = rorId; }

    public String getOaiEndpoint() { return oaiEndpoint; }
    public void setOaiEndpoint(String oaiEndpoint) { this.oaiEndpoint = oaiEndpoint; }

    public String getOaiMetadataPrefix() { return oaiMetadataPrefix; }
    public void setOaiMetadataPrefix(String oaiMetadataPrefix) { this.oaiMetadataPrefix = oaiMetadataPrefix; }

    public String getRebiunCode() { return rebiunCode; }
    public void setRebiunCode(String rebiunCode) { this.rebiunCode = rebiunCode; }

    public LocalDateTime getLastHarvestAt() { return lastHarvestAt; }
    public void setLastHarvestAt(LocalDateTime lastHarvestAt) { this.lastHarvestAt = lastHarvestAt; }

    public OaiStatus getOaiStatus() { return oaiStatus; }
    public void setOaiStatus(OaiStatus oaiStatus) { this.oaiStatus = oaiStatus; }

    public List<Publication> getPublications() { return publications; }
    public void setPublications(List<Publication> publications) { this.publications = publications; }

    public List<HarvestJob> getHarvestJobs() { return harvestJobs; }
    public void setHarvestJobs(List<HarvestJob> harvestJobs) { this.harvestJobs = harvestJobs; }
}
