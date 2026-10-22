package es.oamonitor.service;

import es.oamonitor.dto.ApcEstimateDto;
import es.oamonitor.dto.ApcYearBreakdown;
import es.oamonitor.model.Institution;
import es.oamonitor.model.JournalApcPrice;
import es.oamonitor.model.Publication;
import es.oamonitor.model.enums.ApcSource;
import es.oamonitor.model.enums.OaStatus;
import es.oamonitor.repository.InstitutionRepository;
import es.oamonitor.repository.JournalApcPriceRepository;
import es.oamonitor.repository.PublicationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ApcCalculationService {

    private final PublicationRepository publicationRepository;
    private final JournalApcPriceRepository journalApcPriceRepository;
    private final InstitutionRepository institutionRepository;

    public ApcCalculationService(PublicationRepository publicationRepository,
                                  JournalApcPriceRepository journalApcPriceRepository,
                                  InstitutionRepository institutionRepository) {
        this.publicationRepository = publicationRepository;
        this.journalApcPriceRepository = journalApcPriceRepository;
        this.institutionRepository = institutionRepository;
    }

    public Optional<ApcEstimateDto> calculateApcEstimate(UUID institutionId) {
        return institutionRepository.findById(institutionId)
                .map(this::calculate);
    }

    @org.springframework.transaction.annotation.Transactional
    public Optional<ApcEstimateDto> calculateAndPersist(UUID institutionId) {
        return institutionRepository.findById(institutionId)
                .map(inst -> {
                    ApcEstimateDto dto = calculate(inst);
                    persistApcValues(inst);
                    return dto;
                });
    }

    private void persistApcValues(Institution institution) {
        UUID instId = institution.getId();
        List<Publication> pubs = publicationRepository
                .findByInstitutionIdAndOaStatusResolvedIn(instId, List.of(OaStatus.GOLD, OaStatus.HYBRID));

        for (Publication pub : pubs) {
            if (pub.getJournalIssn() != null && pub.getPublicationYear() != null) {
                Optional<JournalApcPrice> priceOpt = findPriceForYear(pub.getJournalIssn(), pub.getPublicationYear());
                if (priceOpt.isPresent()) {
                    JournalApcPrice price = priceOpt.get();
                    if (price.getApcEur() != null) {
                        pub.setApcEstimatedEur(price.getApcEur());
                        pub.setApcSource(ApcSource.OPENAPC);
                    }
                }
            }
            publicationRepository.save(pub);
        }
    }

    private ApcEstimateDto calculate(Institution institution) {
        UUID instId = institution.getId();

        List<Publication> pubs = publicationRepository
                .findByInstitutionIdAndOaStatusResolvedIn(instId, List.of(OaStatus.GOLD, OaStatus.HYBRID));

        Map<Integer, ApcYearBreakdown> breakdownByYear = new TreeMap<>(Comparator.reverseOrder());
        BigDecimal totalCost = BigDecimal.ZERO;
        int withPrice = 0;

        for (Publication pub : pubs) {
            BigDecimal cost = BigDecimal.ZERO;
            boolean hasPrice = false;

            if (pub.getJournalIssn() != null && pub.getPublicationYear() != null) {
                Optional<JournalApcPrice> priceOpt = findPriceForYear(pub.getJournalIssn(), pub.getPublicationYear());
                if (priceOpt.isPresent()) {
                    JournalApcPrice price = priceOpt.get();
                    if (price.getApcEur() != null) {
                        cost = price.getApcEur();
                        hasPrice = true;
                    }
                }
            }

            totalCost = totalCost.add(cost);
            if (hasPrice) withPrice++;

            int year = pub.getPublicationYear() != null ? pub.getPublicationYear() : 0;
            ApcYearBreakdown existing = breakdownByYear.get(year);
            if (existing == null) {
                breakdownByYear.put(year, new ApcYearBreakdown(year, cost, 1, hasPrice ? 1 : 0));
            } else {
                breakdownByYear.put(year, new ApcYearBreakdown(
                        year,
                        existing.totalCost().add(cost),
                        existing.publicationsCount() + 1,
                        existing.publicationsWithPrice() + (hasPrice ? 1 : 0)
                ));
            }
        }

        return new ApcEstimateDto(
                institution.getId(),
                institution.getName(),
                totalCost,
                pubs.size(),
                withPrice,
                pubs.size() - withPrice,
                breakdownByYear
        );
    }

    private Optional<JournalApcPrice> findPriceForYear(String issn, Integer year) {
        Optional<JournalApcPrice> exact = journalApcPriceRepository.findByIssnAndYear(issn, year);
        if (exact.isPresent()) {
            return exact;
        }
        List<JournalApcPrice> allForIssn = journalApcPriceRepository.findAllByIssnOrderByYearDesc(issn);
        if (allForIssn.isEmpty()) {
            return Optional.empty();
        }
        for (JournalApcPrice price : allForIssn) {
            if (price.getYear() <= year) {
                return Optional.of(price);
            }
        }
        return Optional.of(allForIssn.get(0));
    }
}
