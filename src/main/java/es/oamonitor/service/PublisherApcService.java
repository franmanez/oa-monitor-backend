package es.oamonitor.service;

import es.oamonitor.dto.PublisherArticleDto;
import es.oamonitor.dto.PublisherApcDto;
import es.oamonitor.model.JournalApcPrice;
import es.oamonitor.model.Publication;
import es.oamonitor.model.enums.OaStatus;
import es.oamonitor.repository.JournalApcPriceRepository;
import es.oamonitor.repository.PublicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PublisherApcService {

    static final String UNKNOWN_PUBLISHER = "Sin especificar";

    private final PublicationRepository publicationRepository;
    private final JournalApcPriceRepository journalApcPriceRepository;

    public PublisherApcService(PublicationRepository publicationRepository,
                               JournalApcPriceRepository journalApcPriceRepository) {
        this.publicationRepository = publicationRepository;
        this.journalApcPriceRepository = journalApcPriceRepository;
    }

    @Transactional(readOnly = true)
    public List<PublisherApcDto> getPublishersApc(UUID institutionId, Integer year) {
        List<Publication> pubs = publicationRepository.findWithApcByInstitution(institutionId, year);
        Map<String, String> publisherByPubId = resolvePublishers(pubs);

        Map<String, List<Publication>> byPublisher = new LinkedHashMap<>();
        for (Publication pub : pubs) {
            String publisher = publisherByPubId.getOrDefault(pub.getId().toString(), UNKNOWN_PUBLISHER);
            byPublisher.computeIfAbsent(publisher, k -> new ArrayList<>()).add(pub);
        }

        BigDecimal grandTotal = byPublisher.values().stream()
                .flatMap(Collection::stream)
                .map(Publication::getApcEstimatedEur)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PublisherApcDto> result = new ArrayList<>();
        for (Map.Entry<String, List<Publication>> entry : byPublisher.entrySet()) {
            List<BigDecimal> values = entry.getValue().stream()
                    .map(Publication::getApcEstimatedEur)
                    .toList();
            BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal average = total.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
            BigDecimal stdDev = standardDeviation(values);
            double percentage = grandTotal.signum() > 0
                    ? Math.round(total.doubleValue() / grandTotal.doubleValue() * 10000.0) / 100.0
                    : 0.0;

            // El ranking solo incluye artículos con APC, cuyo estado resuelto
            // es GOLD o HYBRID; se desglosan para la gráfica Oro vs Híbrido
            long gold = 0, hybrid = 0;
            for (Publication pub : entry.getValue()) {
                if (pub.getOaStatusResolved() == OaStatus.GOLD) gold++;
                else if (pub.getOaStatusResolved() == OaStatus.HYBRID) hybrid++;
            }

            result.add(new PublisherApcDto(entry.getKey(), total, values.size(), average, stdDev, percentage,
                    gold, hybrid));
        }
        result.sort(Comparator.comparing(PublisherApcDto::totalApc).reversed());
        return result;
    }

    @Transactional(readOnly = true)
    public List<PublisherArticleDto> getPublisherArticles(UUID institutionId, Integer year, String publisher) {
        List<Publication> pubs = publicationRepository.findWithApcByInstitution(institutionId, year);
        Map<String, String> publisherByPubId = resolvePublishers(pubs);

        return pubs.stream()
                .filter(p -> publisherByPubId.getOrDefault(p.getId().toString(), UNKNOWN_PUBLISHER).equals(publisher))
                .sorted(Comparator.comparing(Publication::getApcEstimatedEur).reversed())
                .map(p -> new PublisherArticleDto(
                        p.getId().toString(),
                        p.getDoi(),
                        p.getTitle(),
                        p.getJournalName(),
                        p.getJournalIssn(),
                        p.getApcEstimatedEur(),
                        p.getOaStatusResolved() != null ? p.getOaStatusResolved().name() : null))
                .collect(Collectors.toList());
    }

    private Map<String, String> resolvePublishers(List<Publication> pubs) {
        Map<String, String> publisherByPubId = new HashMap<>();
        if (pubs.isEmpty()) return publisherByPubId;

        Set<String> issns = pubs.stream()
                .map(Publication::getJournalIssn)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (issns.isEmpty()) return publisherByPubId;

        Map<String, List<JournalApcPrice>> pricesByIssn = journalApcPriceRepository.findByIssnIn(issns).stream()
                .collect(Collectors.groupingBy(JournalApcPrice::getIssn));

        for (Publication pub : pubs) {
            if (pub.getJournalIssn() == null) continue;
            List<JournalApcPrice> rows = pricesByIssn.get(pub.getJournalIssn());
            if (rows == null || rows.isEmpty()) continue;
            JournalApcPrice best = findClosestByYear(rows, pub.getPublicationYear());
            if (best != null && best.getPublisher() != null && !best.getPublisher().isBlank()) {
                publisherByPubId.put(pub.getId().toString(), best.getPublisher());
            }
        }
        return publisherByPubId;
    }

    private JournalApcPrice findClosestByYear(List<JournalApcPrice> rows, Integer year) {
        List<JournalApcPrice> sorted = rows.stream()
                .filter(r -> r.getYear() != null)
                .sorted(Comparator.comparing(JournalApcPrice::getYear).reversed())
                .toList();
        if (sorted.isEmpty()) return rows.get(0);
        if (year == null) return sorted.get(0);
        for (JournalApcPrice row : sorted) {
            if (row.getYear() <= year) return row;
        }
        return sorted.get(0);
    }

    private BigDecimal standardDeviation(List<BigDecimal> values) {
        int n = values.size();
        if (n < 2) return null;
        double mean = values.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0.0);
        double sumSquares = values.stream()
                .mapToDouble(v -> Math.pow(v.doubleValue() - mean, 2))
                .sum();
        double std = Math.sqrt(sumSquares / (n - 1));
        return BigDecimal.valueOf(std).setScale(2, RoundingMode.HALF_UP);
    }
}
