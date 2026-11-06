package es.oamonitor.service;

import es.oamonitor.dto.*;
import es.oamonitor.model.Institution;
import es.oamonitor.model.enums.OaStatus;
import es.oamonitor.repository.InstitutionRepository;
import es.oamonitor.repository.PublicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HistoricalService {

    private final InstitutionRepository institutionRepository;
    private final PublicationRepository publicationRepository;
    private final ApcCalculationService apcCalculationService;

    public HistoricalService(InstitutionRepository institutionRepository,
                             PublicationRepository publicationRepository,
                             ApcCalculationService apcCalculationService) {
        this.institutionRepository = institutionRepository;
        this.publicationRepository = publicationRepository;
        this.apcCalculationService = apcCalculationService;
    }

    @Transactional(readOnly = true)
    public Optional<HistoricalDataDto> getHistoricalData(UUID institutionId) {
        return institutionRepository.findById(institutionId)
                .map(inst -> {
                    UUID instId = inst.getId();

                    // Obtener datos OA por año (query eficiente)
                    List<Object[]> oaRows = publicationRepository.aggregateHistoricalByInstitution(instId);
                    // Obtener datos APC por año
                    List<Object[]> apcRows = publicationRepository.aggregateApcHistoricalByInstitution(instId);

                    Map<Integer, Object[]> apcByYear = new HashMap<>();
                    for (Object[] row : apcRows) {
                        apcByYear.put((Integer) row[0], row);
                    }

                    List<HistoricalYearDto> yearlyData = new ArrayList<>();

                    for (Object[] row : oaRows) {
                        int year = (Integer) row[0];
                        long total = (Long) row[1];
                        long diamond = (Long) row[2];
                        long gold = (Long) row[3];
                        long green = (Long) row[4];
                        long hybrid = (Long) row[5];
                        long bronze = (Long) row[6];
                        long closed = (Long) row[7];
                        long repository = (Long) row[8];

                        long strongOaCount = diamond + gold + green;
                        long broadOaCount = total - closed;

                        double oaPercentage = total > 0 ? Math.round((double) broadOaCount / total * 1000.0) / 10.0 : 0.0;
                        double strongOaPct = total > 0 ? Math.round((double) strongOaCount / total * 1000.0) / 10.0 : 0.0;
                        double repositoryPct = total > 0 ? Math.round((double) repository / total * 1000.0) / 10.0 : 0.0;
                        double broadOaPct = oaPercentage;

                        double complianceScore = Math.round((strongOaPct * 0.4 + repositoryPct * 0.3 + broadOaPct * 0.3) * 10.0) / 10.0;
                        String complianceGrade = calculateGrade(complianceScore);

                        // APC data for this year
                        double totalApcCost = 0.0;
                        int articlesWithApc = 0;
                        Object[] apcRow = apcByYear.get(year);
                        if (apcRow != null) {
                            articlesWithApc = ((Long) apcRow[1]).intValue();
                            totalApcCost = ((java.math.BigDecimal) apcRow[2]).doubleValue();
                        }
                        double averageApc = articlesWithApc > 0 ? Math.round(totalApcCost / articlesWithApc * 100.0) / 100.0 : 0.0;

                        yearlyData.add(new HistoricalYearDto(
                                year,
                                total,
                                diamond,
                                gold,
                                green,
                                hybrid,
                                bronze,
                                closed,
                                repository,
                                oaPercentage,
                                strongOaPct,
                                repositoryPct,
                                broadOaPct,
                                complianceScore,
                                complianceGrade,
                                totalApcCost,
                                articlesWithApc,
                                averageApc
                        ));
                    }

                    return new HistoricalDataDto(
                            inst.getId().toString(),
                            inst.getName(),
                            yearlyData
                    );
                });
    }

    @Transactional(readOnly = true)
    public List<JournalApcDto> getJournalsByApcForYear(UUID institutionId, int year) {
        return publicationRepository.journalApcByInstitutionAndYear(institutionId, year).stream()
                .map(row -> new JournalApcDto(
                        (String) row[0],
                        (String) row[1],
                        (Long) row[2],
                        (java.math.BigDecimal) row[3]
                )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Integer> getAvailableYears(UUID institutionId) {
        return publicationRepository.findDistinctYearsByInstitutionId(institutionId);
    }

    private String calculateGrade(double score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B+";
        if (score >= 60) return "B";
        if (score >= 50) return "C+";
        if (score >= 40) return "C";
        if (score >= 30) return "D";
        return "F";
    }
}
