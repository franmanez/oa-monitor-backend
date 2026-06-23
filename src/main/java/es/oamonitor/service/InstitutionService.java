package es.oamonitor.service;

import es.oamonitor.dto.*;
import es.oamonitor.model.Institution;
import es.oamonitor.model.Publication;
import es.oamonitor.model.enums.OaiStatus;
import es.oamonitor.model.enums.OaStatus;
import es.oamonitor.repository.InstitutionRepository;
import es.oamonitor.repository.PublicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final PublicationRepository publicationRepository;
    private final ApcCalculationService apcCalculationService;

    public InstitutionService(InstitutionRepository institutionRepository,
                              PublicationRepository publicationRepository,
                              ApcCalculationService apcCalculationService) {
        this.institutionRepository = institutionRepository;
        this.publicationRepository = publicationRepository;
        this.apcCalculationService = apcCalculationService;
    }

    @Transactional(readOnly = true)
    public InstitutionPageResponse getAllInstitutions(Pageable pageable) {
        Page<InstitutionDto> page = institutionRepository.findAll(pageable)
                .map(this::toDto);
        return new InstitutionPageResponse(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
    }

    @Transactional(readOnly = true)
    public Optional<InstitutionDto> getById(UUID id) {
        return institutionRepository.findById(id)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<Institution> findEntityById(UUID id) {
        return institutionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<InstitutionSummaryDto> getSummary(UUID id, Integer year) {
        return institutionRepository.findById(id)
                .map(inst -> {
                    UUID instId = inst.getId();
                    long total;
                    Map<String, Long> statusCounts = new LinkedHashMap<>();
                    long foundRepo;

                    if (year != null) {
                        total = publicationRepository.countByInstitutionIdAndPublicationYear(instId, year);
                        for (OaStatus status : OaStatus.values()) {
                            long count = publicationRepository.countByInstitutionIdAndOaStatusResolvedAndPublicationYear(instId, status, year);
                            statusCounts.put(status.name(), count);
                        }
                        foundRepo = publicationRepository.countByInstitutionIdAndFoundInRepositoryTrueAndYear(instId, year);
                    } else {
                        total = publicationRepository.countByInstitutionId(instId);
                        for (OaStatus status : OaStatus.values()) {
                            long count = publicationRepository.countByInstitutionIdAndOaStatusResolved(instId, status);
                            statusCounts.put(status.name(), count);
                        }
                        foundRepo = publicationRepository.countByInstitutionIdAndFoundInRepositoryTrue(instId);
                    }

                    long totalOpen = total - statusCounts.getOrDefault(OaStatus.CLOSED.name(), 0L);
                    double oaPct = total > 0 ? (double) totalOpen / total * 100.0 : 0.0;

                    List<Integer> years = publicationRepository.findDistinctYearsByInstitutionId(instId);
                    Map<Integer, Map<String, Long>> yearlyBreakdown = new LinkedHashMap<>();
                    for (int y : years) {
                        List<Object[]> yearStats = publicationRepository.statusCountByInstitutionAndYear(instId, y);
                        Map<String, Long> yearMap = new LinkedHashMap<>();
                        for (OaStatus status : OaStatus.values()) {
                            yearMap.put(status.name(), 0L);
                        }
                        for (Object[] row : yearStats) {
                            OaStatus status = (OaStatus) row[0];
                            long count = (Long) row[1];
                            yearMap.put(status.name(), count);
                        }
                        yearlyBreakdown.put(y, yearMap);
                    }

                    return new InstitutionSummaryDto(
                            inst.getId().toString(),
                            inst.getName(),
                            total,
                            statusCounts,
                            Math.round(oaPct * 10.0) / 10.0,
                            foundRepo,
                            yearlyBreakdown
                    );
                });
    }

    @Transactional(readOnly = true)
    public Optional<OaBreakdownDto> getOaBreakdown(UUID id) {
        return institutionRepository.findById(id)
                .map(inst -> {
                    UUID instId = inst.getId();
                    List<Integer> years = publicationRepository.findDistinctYearsByInstitutionId(instId);

                    Map<Integer, Map<String, Long>> yearlyCounts = new LinkedHashMap<>();
                    Map<Integer, Double> yearlyOaPct = new TreeMap<>();

                    for (int year : years) {
                        List<Object[]> yearStats = publicationRepository.statusCountByInstitutionAndYear(instId, year);
                        Map<String, Long> yearMap = new LinkedHashMap<>();
                        long yearTotal = 0;
                        for (OaStatus status : OaStatus.values()) {
                            yearMap.put(status.name(), 0L);
                        }
                        for (Object[] row : yearStats) {
                            OaStatus status = (OaStatus) row[0];
                            long count = (Long) row[1];
                            yearMap.put(status.name(), count);
                            yearTotal += count;
                        }
                        yearlyCounts.put(year, yearMap);

                        long closed = yearMap.getOrDefault(OaStatus.CLOSED.name(), 0L);
                        yearlyOaPct.put(year, yearTotal > 0
                                ? Math.round((double) (yearTotal - closed) / yearTotal * 1000.0) / 10.0
                                : 0.0);
                    }

                    return new OaBreakdownDto(inst.getId().toString(), inst.getName(), yearlyCounts, yearlyOaPct);
                });
    }

    @Transactional
    public InstitutionDto createInstitution(CreateInstitutionRequest request) {
        if (institutionRepository.existsByRorId(request.rorId())) {
            throw new IllegalArgumentException("Institution with ROR " + request.rorId() + " already exists");
        }

        Institution institution = new Institution();
        institution.setName(request.name());
        institution.setRorId(request.rorId());
        institution.setOaiEndpoint(request.oaiEndpoint());
        institution.setOaiMetadataPrefix(request.oaiMetadataPrefix());
        institution.setRebiunCode(request.rebiunCode());
        institution.setOaiStatus(OaiStatus.ACTIVE);

        Institution saved = institutionRepository.save(institution);
        return toDto(saved);
    }

    @Transactional
    public Optional<InstitutionDto> updateOaiConfig(UUID id, String oaiEndpoint, String oaiMetadataPrefix) {
        return institutionRepository.findById(id)
                .map(inst -> {
                    inst.setOaiEndpoint(oaiEndpoint);
                    inst.setOaiMetadataPrefix(oaiMetadataPrefix);
                    return toDto(institutionRepository.save(inst));
                });
    }

    @Transactional
    public void updateInstitutionAfterHarvest(Institution institution) {
        institution.setLastHarvestAt(java.time.LocalDateTime.now());
        institutionRepository.save(institution);
    }

    @Transactional(readOnly = true)
    public Optional<ApcEstimateDto> getApcEstimate(UUID id) {
        return apcCalculationService.calculateApcEstimate(id);
    }

    @Transactional
    public Optional<ApcEstimateDto> calculateAndPersistApc(UUID id) {
        return apcCalculationService.calculateAndPersist(id);
    }

    @Transactional(readOnly = true)
    public List<InstitutionRankingDto> getRanking(Integer year) {
        List<Object[]> rows;
        if (year != null) {
            rows = publicationRepository.aggregateByInstitutionAndYear(year);
        } else {
            rows = publicationRepository.aggregateByInstitution();
        }
        return rows.stream().map(row -> {
            String id = ((UUID) row[0]).toString();
            String name = (String) row[1];
            long total = (Long) row[2];
            long open = (Long) row[3];
            double oaPct = total > 0 ? Math.round((double) open / total * 1000.0) / 10.0 : 0.0;
            long closed = (Long) row[4];
            return new InstitutionRankingDto(id, name, total, oaPct, open, closed);
        }).sorted(Comparator.comparingDouble(InstitutionRankingDto::oaPercentage).reversed())
         .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Integer> getAvailableRankingYears() {
        return publicationRepository.findDistinctYearsAll();
    }

    @Transactional(readOnly = true)
    public Page<PublicationDto> getPublicationsFiltered(UUID institutionId, Integer year, String status, String search, Pageable pageable) {
        OaStatus statusEnum = status != null && !status.isEmpty() ? OaStatus.valueOf(status) : null;
        Page<Publication> page = publicationRepository.findFiltered(institutionId, year, statusEnum, search, pageable);
        return page.map(this::toPublicationDto);
    }

    private PublicationDto toPublicationDto(Publication pub) {
        return new PublicationDto(
            pub.getId().toString(),
            pub.getDoi(),
            pub.getTitle(),
            pub.getPublicationYear(),
            pub.getJournalIssn(),
            pub.getJournalName(),
            pub.getOaStatusOpenalex() != null ? pub.getOaStatusOpenalex().name() : null,
            pub.getOaStatusResolved() != null ? pub.getOaStatusResolved().name() : null,
            pub.getFoundInRepository(),
            pub.getRepositoryUrl(),
            pub.getApcEstimatedEur(),
            pub.getApcCoveredByAgreement()
        );
    }

    @Transactional(readOnly = true)
    public List<Publication> getPublicationsForExport(UUID institutionId, Integer year) {
        if (year != null) {
            return publicationRepository.findAllByInstitutionIdAndYear(institutionId, year);
        }
        return publicationRepository.findAllByInstitutionId(institutionId);
    }

    @Transactional(readOnly = true)
    public NationalStatsDto getNationalStats() {
        long totalPubs = publicationRepository.nationalTotalPublications();
        long totalInst = institutionRepository.count();

        List<Object[]> statusRows = publicationRepository.nationalStatusCounts();
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (OaStatus status : OaStatus.values()) {
            statusCounts.put(status.name(), 0L);
        }
        for (Object[] row : statusRows) {
            OaStatus status = (OaStatus) row[0];
            long count = (Long) row[1];
            statusCounts.put(status.name(), count);
        }

        long totalClosed = statusCounts.getOrDefault(OaStatus.CLOSED.name(), 0L);
        long totalOpen = totalPubs - totalClosed;
        double oaPct = totalPubs > 0 ? Math.round((double) totalOpen / totalPubs * 1000.0) / 10.0 : 0.0;

        return new NationalStatsDto(
            totalPubs, totalInst, oaPct,
            totalOpen, totalClosed, statusCounts
        );
    }

    @Transactional(readOnly = true)
    public List<JournalApcDto> getJournalsByApc(UUID institutionId, Integer year) {
        return publicationRepository.journalApcByInstitution(institutionId, year).stream()
                .map(row -> new JournalApcDto(
                        (String) row[0],
                        (String) row[1],
                        (Long) row[2],
                        (java.math.BigDecimal) row[3]
                )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JournalCountDto> getTopJournals(UUID institutionId, Integer year, int limit) {
        return publicationRepository.topJournalsByInstitution(institutionId, year).stream()
                .limit(limit)
                .map(row -> new JournalCountDto(
                        (String) row[0],
                        (String) row[1],
                        (Long) row[2]
                )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InstitutionScatterDto> getInstitutionScatter(int year) {
        return publicationRepository.aggregateInstitutionScatter(year).stream()
                .map(row -> {
                    String id = ((UUID) row[0]).toString();
                    String name = (String) row[1];
                    long total = (Long) row[2];
                    long open = (Long) row[3];
                    java.math.BigDecimal apcCost = (java.math.BigDecimal) row[4];
                    long withApc = (Long) row[5];
                    double oaPct = total > 0 ? Math.round((double) open / total * 1000.0) / 10.0 : 0.0;
                    return new InstitutionScatterDto(id, name, total, oaPct, apcCost, withApc > 0);
                }).collect(Collectors.toList());
    }

    private InstitutionDto toDto(Institution inst) {
        long total = publicationRepository.countByInstitutionId(inst.getId());
        return new InstitutionDto(
                inst.getId().toString(),
                inst.getName(),
                inst.getRorId(),
                inst.getOaiEndpoint(),
                inst.getOaiMetadataPrefix(),
                inst.getRebiunCode(),
                inst.getLastHarvestAt(),
                inst.getOaiStatus(),
                total
        );
    }
}
