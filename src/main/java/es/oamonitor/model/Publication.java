package es.oamonitor.model;

import es.oamonitor.model.enums.ApcSource;
import es.oamonitor.model.enums.OaStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "publication", indexes = {
    @Index(columnList = "doi"),
    @Index(columnList = "institution_id")
})
public class Publication {

    @Id
    @Convert(converter = BinaryUuidConverter.class)
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column
    private String doi;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "journal_issn", length = 20)
    private String journalIssn;

    @Column(name = "journal_name")
    private String journalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "oa_status_openalex")
    private OaStatus oaStatusOpenalex;

    @Enumerated(EnumType.STRING)
    @Column(name = "oa_status_resolved")
    private OaStatus oaStatusResolved;

    @Column(name = "found_in_repository")
    private Boolean foundInRepository = false;

    @Column(name = "repository_url", length = 1024)
    private String repositoryUrl;

    @Column(name = "embargo_end_date")
    private LocalDate embargoEndDate;

    @Column(name = "apc_estimated_eur", precision = 10, scale = 2)
    private BigDecimal apcEstimatedEur;

    @Enumerated(EnumType.STRING)
    @Column(name = "apc_source")
    private ApcSource apcSource;

    @Column(name = "apc_covered_by_agreement")
    private Boolean apcCoveredByAgreement = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public String getDoi() { return doi; }
    public void setDoi(String doi) { this.doi = doi; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public String getJournalIssn() { return journalIssn; }
    public void setJournalIssn(String journalIssn) { this.journalIssn = journalIssn; }

    public String getJournalName() { return journalName; }
    public void setJournalName(String journalName) { this.journalName = journalName; }

    public OaStatus getOaStatusOpenalex() { return oaStatusOpenalex; }
    public void setOaStatusOpenalex(OaStatus oaStatusOpenalex) { this.oaStatusOpenalex = oaStatusOpenalex; }

    public OaStatus getOaStatusResolved() { return oaStatusResolved; }
    public void setOaStatusResolved(OaStatus oaStatusResolved) { this.oaStatusResolved = oaStatusResolved; }

    public Boolean getFoundInRepository() { return foundInRepository; }
    public void setFoundInRepository(Boolean foundInRepository) { this.foundInRepository = foundInRepository; }

    public String getRepositoryUrl() { return repositoryUrl; }
    public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; }

    public LocalDate getEmbargoEndDate() { return embargoEndDate; }
    public void setEmbargoEndDate(LocalDate embargoEndDate) { this.embargoEndDate = embargoEndDate; }

    public BigDecimal getApcEstimatedEur() { return apcEstimatedEur; }
    public void setApcEstimatedEur(BigDecimal apcEstimatedEur) { this.apcEstimatedEur = apcEstimatedEur; }

    public ApcSource getApcSource() { return apcSource; }
    public void setApcSource(ApcSource apcSource) { this.apcSource = apcSource; }

    public Boolean getApcCoveredByAgreement() { return apcCoveredByAgreement; }
    public void setApcCoveredByAgreement(Boolean apcCoveredByAgreement) { this.apcCoveredByAgreement = apcCoveredByAgreement; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
