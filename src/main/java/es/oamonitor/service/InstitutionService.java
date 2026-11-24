package es.oamonitor.service;

import es.oamonitor.dto.*;
import es.oamonitor.model.Institution;
import es.oamonitor.model.Publication;
import es.oamonitor.model.enums.OaiStatus;
import es.oamonitor.model.enums.OaStatus;
import es.oamonitor.repository.InstitutionRepository;
import es.oamonitor.repository.PublicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final PublicationRepository publicationRepository;
    private final ApcCalculationService apcCalculationService;
    private final PublisherApcService publisherApcService;

    public InstitutionService(InstitutionRepository institutionRepository,
                              PublicationRepository publicationRepository,
                              ApcCalculationService apcCalculationService,
                              PublisherApcService publisherApcService) {
        this.institutionRepository = institutionRepository;
        this.publicationRepository = publicationRepository;
        this.apcCalculationService = apcCalculationService;
        this.publisherApcService = publisherApcService;
    }

    @Transactional(readOnly = true)
    public InstitutionPageResponse getAllInstitutions(Pageable pageable) {
        Page<InstitutionDto> page = institutionRepository.findByOaiStatus(OaiStatus.ACTIVE, pageable)
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
    public List<InstitutionDto> getAllForAdmin() {
        return institutionRepository.findAllByOrderByNameAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<InstitutionDto> updateOaiStatus(UUID id, OaiStatus status) {
        return institutionRepository.findById(id)
                .map(inst -> {
                    inst.setOaiStatus(status);
                    return toDto(institutionRepository.save(inst));
                });
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
    public Optional<InstitutionDto> updateActivationData(UUID id, String rorId, String oaiEndpoint, String oaiMetadataPrefix) {
        return institutionRepository.findById(id)
                .map(inst -> {
                    if (rorId != null && !rorId.isBlank()) {
                        inst.setRorId(rorId);
                    }
                    if (oaiEndpoint != null && !oaiEndpoint.isBlank()) {
                        inst.setOaiEndpoint(oaiEndpoint);
                    }
                    if (oaiMetadataPrefix != null && !oaiMetadataPrefix.isBlank()) {
                        inst.setOaiMetadataPrefix(oaiMetadataPrefix);
                    }
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
        Page<Publication> page = publicationRepository.findFiltered(institutionId, year, statusEnum, search, publicationPageable(pageable));
        return page.map(this::toPublicationDto);
    }

    private Pageable publicationPageable(Pageable pageable) {
        Sort base = Sort.by(Sort.Order.desc("publicationYear"), Sort.Order.asc("title"));
        Sort apcOrder = null;
        for (Sort.Order order : pageable.getSort()) {
            if ("apcEstimatedEur".equalsIgnoreCase(order.getProperty())) {
                apcOrder = order.isDescending()
                        ? JpaSort.unsafe(Sort.Direction.DESC, "COALESCE(p.apcEstimatedEur, -1)")
                        : JpaSort.unsafe(Sort.Direction.ASC, "COALESCE(p.apcEstimatedEur, 999999999)");
                break;
            }
        }
        Sort sort = apcOrder != null ? apcOrder.and(base) : base;
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
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
            pub.getRepositoryUrl(),
            pub.getSource() != null ? pub.getSource().name() : null,
            pub.getApcEstimatedEur(),
            pub.getField(),
            pub.getSubfield(),
            pub.getTopic()
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
    public List<JournalApcDto> getJournalsByApc(UUID institutionId, Integer year, Integer limit) {
        var rows = publicationRepository.journalApcByInstitution(institutionId, year).stream();
        if (limit != null && limit > 0) rows = rows.limit(limit);
        return rows
                .map(row -> new JournalApcDto(
                        (String) row[0],
                        (String) row[1],
                        (Long) row[2],
                        (java.math.BigDecimal) row[3]
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

    @Transactional(readOnly = true)
    public List<PublisherApcDto> getPublishersApc(UUID institutionId, Integer year) {
        return publisherApcService.getPublishersApc(institutionId, year);
    }

    @Transactional(readOnly = true)
    public List<PublisherArticleDto> getPublisherArticles(UUID institutionId, Integer year, String publisher) {
        return publisherApcService.getPublisherArticles(institutionId, year, publisher);
    }

    @Transactional(readOnly = true)
    public List<JournalArticleDto> getJournalArticles(UUID institutionId, Integer year, String journal) {
        List<Publication> pubs = publicationRepository.findWithApcByInstitution(institutionId, year);
        return pubs.stream()
                .filter(p -> journal.equals(p.getJournalName() == null || p.getJournalName().isBlank()
                        ? "Sin nombre" : p.getJournalName()))
                .sorted(Comparator.comparing(Publication::getApcEstimatedEur).reversed())
                .map(p -> new JournalArticleDto(
                        p.getId().toString(),
                        p.getDoi(),
                        p.getTitle(),
                        p.getJournalName(),
                        p.getJournalIssn(),
                        p.getApcEstimatedEur(),
                        p.getOaStatusResolved() != null ? p.getOaStatusResolved().name() : null))
                .collect(Collectors.toList());
    }

    // ---- Áreas temáticas (primary_topic.field de OpenAlex) ----

    private static final String UNCLASSIFIED_FIELD = "Sin clasificar";

    @Transactional(readOnly = true)
    public List<FieldStatsDto> getFieldStats(UUID institutionId, Integer year) {
        List<Publication> pubs = publicationRepository.findAllByInstitutionIdAndYear(institutionId, year);
        if (pubs.isEmpty()) return List.of();

        Map<String, List<Publication>> byField = new LinkedHashMap<>();
        for (Publication p : pubs) {
            String field = p.getField() == null || p.getField().isBlank() ? UNCLASSIFIED_FIELD : p.getField();
            byField.computeIfAbsent(field, k -> new ArrayList<>()).add(p);
        }

        long total = pubs.size();
        return byField.entrySet().stream().map(entry -> {
            List<Publication> list = entry.getValue();
            long count = list.size();
            long diamond = 0, gold = 0, green = 0, hybrid = 0, bronze = 0, closed = 0, repo = 0, withApc = 0;
            BigDecimal apcTotal = BigDecimal.ZERO;
            for (Publication p : list) {
                OaStatus status = p.getOaStatusResolved();
                if (status == null) {
                    closed++;
                } else {
                    switch (status) {
                        case DIAMOND -> diamond++;
                        case GOLD -> gold++;
                        case GREEN -> green++;
                        case HYBRID -> hybrid++;
                        case BRONZE -> bronze++;
                        case CLOSED -> closed++;
                    }
                }
                if (p.getRepositoryUrl() != null) repo++;
                if (p.getApcEstimatedEur() != null) {
                    apcTotal = apcTotal.add(p.getApcEstimatedEur());
                    withApc++;
                }
            }
            double broadOaPct = round1((count - closed) * 100.0 / count);
            double strongOaPct = round1((diamond + gold + green) * 100.0 / count);
            double repositoryPct = round1(repo * 100.0 / count);
            BigDecimal averageApc = withApc > 0
                    ? apcTotal.divide(BigDecimal.valueOf(withApc), 0, RoundingMode.HALF_UP)
                    : null;
            return new FieldStatsDto(
                    entry.getKey(), count, round1(count * 100.0 / total),
                    diamond, gold, green, hybrid, bronze, closed,
                    broadOaPct, strongOaPct, repo, repositoryPct,
                    apcTotal, withApc, averageApc);
        })
        .sorted(Comparator.comparingLong(FieldStatsDto::articleCount).reversed())
        .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FieldArticleDto> getFieldArticles(UUID institutionId, Integer year, String field) {
        List<Publication> pubs = publicationRepository.findAllByInstitutionIdAndYear(institutionId, year);
        return pubs.stream()
                .filter(p -> field.equals(p.getField() == null || p.getField().isBlank()
                        ? UNCLASSIFIED_FIELD : p.getField()))
                .sorted(Comparator.comparing(
                        (Publication p) -> p.getApcEstimatedEur() != null ? p.getApcEstimatedEur() : BigDecimal.ZERO,
                        Comparator.reverseOrder()))
                .limit(500)
                .map(p -> new FieldArticleDto(
                        p.getId().toString(),
                        p.getDoi(),
                        p.getTitle(),
                        p.getJournalName(),
                        p.getPublicationYear(),
                        p.getSubfield(),
                        p.getTopic(),
                        p.getOaStatusResolved() != null ? p.getOaStatusResolved().name() : null,
                        p.getApcEstimatedEur()))
                .collect(Collectors.toList());
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public List<RegionalStatsDto> getRegionalStats(Integer year) {        List<Object[]> rows;
        if (year != null) {
            rows = publicationRepository.aggregateByCommunityAndYear(year);
        } else {
            rows = publicationRepository.aggregateByCommunity();
        }
        return rows.stream().map(row -> {
            String community = (String) row[0];
            int universityCount = ((Long) row[1]).intValue();
            long total = (Long) row[2];
            long diamond = (Long) row[3];
            long gold = (Long) row[4];
            long green = (Long) row[5];
            long hybrid = (Long) row[6];
            long bronze = (Long) row[7];
            long closed = (Long) row[8];
            long repo = (Long) row[9];

            long strongOa = diamond + gold + green;
            long broadOa = total - closed;

            double oaPct = total > 0 ? Math.round((double) broadOa / total * 1000.0) / 10.0 : 0.0;
            double strongOaPct = total > 0 ? Math.round((double) strongOa / total * 1000.0) / 10.0 : 0.0;
            double repositoryPct = total > 0 ? Math.round((double) repo / total * 1000.0) / 10.0 : 0.0;
            double diamondPct = total > 0 ? Math.round((double) diamond / total * 1000.0) / 10.0 : 0.0;
            double goldPct = total > 0 ? Math.round((double) gold / total * 1000.0) / 10.0 : 0.0;
            double greenPct = total > 0 ? Math.round((double) green / total * 1000.0) / 10.0 : 0.0;
            double hybridPct = total > 0 ? Math.round((double) hybrid / total * 1000.0) / 10.0 : 0.0;
            double bronzePct = total > 0 ? Math.round((double) bronze / total * 1000.0) / 10.0 : 0.0;
            double closedPct = total > 0 ? Math.round((double) closed / total * 1000.0) / 10.0 : 0.0;

            return new RegionalStatsDto(
                    community,
                    universityCount,
                    total,
                    oaPct,
                    strongOaPct,
                    repositoryPct,
                    diamondPct,
                    goldPct,
                    greenPct,
                    hybridPct,
                    bronzePct,
                    closedPct
            );
        }).sorted(Comparator.comparingDouble(RegionalStatsDto::oaPercentage).reversed())
         .collect(Collectors.toList());
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
                inst.getAutonomousCommunity(),
                total
        );
    }
}
