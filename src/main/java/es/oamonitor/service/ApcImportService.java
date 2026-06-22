package es.oamonitor.service;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import es.oamonitor.dto.ApcImportResult;
import es.oamonitor.model.JournalApcPrice;
import es.oamonitor.repository.JournalApcPriceRepository;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

@Service
public class ApcImportService {

    private static final String OPENAPC_URL =
            "https://raw.githubusercontent.com/OpenAPC/openapc-de/master/data/apc_de.csv";

    private final JournalApcPriceRepository repository;

    public ApcImportService(JournalApcPriceRepository repository) {
        this.repository = repository;
    }

    public ApcImportResult importFromOpenApc() {
        long start = System.currentTimeMillis();
        int totalRows = 0;
        int skippedErrors = 0;

        Map<String, List<CsvRow>> grouped = new HashMap<>();

        try {
            URI uri = URI.create(OPENAPC_URL);
            CSVReader reader = new CSVReaderBuilder(new InputStreamReader(uri.toURL().openStream()))
                    .withCSVParser(new CSVParserBuilder()
                            .withSeparator(',')
                            .build())
                    .build();

            String[] header = reader.readNext();
            if (header == null) {
                return new ApcImportResult("ERROR", 0, 0, 0, 0, 0);
            }

            Map<String, Integer> colIndex = new HashMap<>();
            for (int i = 0; i < header.length; i++) {
                colIndex.put(header[i].trim().toLowerCase(), i);
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                totalRows++;
                try {
                    String issns = getCol(line, colIndex, "issn");
                    String euroStr = getCol(line, colIndex, "euro");
                    String journalName = getCol(line, colIndex, "journal_full_title");
                    String publisher = getCol(line, colIndex, "publisher");
                    String agreement = getCol(line, colIndex, "agreement");

                    if (issns == null || issns.isBlank()) {
                        skippedErrors++;
                        continue;
                    }

                    BigDecimal euro;
                    try {
                        double val = Double.parseDouble(euroStr);
                        if (val <= 0) { skippedErrors++; continue; }
                        euro = BigDecimal.valueOf(val);
                    } catch (Exception e) {
                        skippedErrors++;
                        continue;
                    }

                    List<String> issnList = Arrays.asList(issns.split(";"));
                    for (String issn : issnList) {
                        issn = issn.trim();
                        if (issn.isBlank()) continue;
                        grouped.computeIfAbsent(issn, k -> new ArrayList<>())
                                .add(new CsvRow(journalName, publisher, euro, agreement != null ? agreement : ""));
                    }
                } catch (Exception e) {
                    skippedErrors++;
                }
            }
            reader.close();

        } catch (Exception e) {
            return new ApcImportResult("ERROR: " + e.getMessage(), totalRows, 0, 0, skippedErrors,
                    (System.currentTimeMillis() - start) / 1000.0);
        }

        List<JournalApcPrice> prices = new ArrayList<>();
        for (Map.Entry<String, List<CsvRow>> entry : grouped.entrySet()) {
            List<CsvRow> rows = entry.getValue();
            BigDecimal sum = BigDecimal.ZERO;
            Map<String, Integer> nameCount = new HashMap<>();
            Map<String, Integer> pubCount = new HashMap<>();
            Set<String> agreements = new HashSet<>();

            for (CsvRow row : rows) {
                sum = sum.add(row.euro);
                nameCount.merge(row.journalName, 1, Integer::sum);
                pubCount.merge(row.publisher, 1, Integer::sum);
                if (!row.agreement.isBlank()) {
                    agreements.add(row.agreement);
                }
            }

            BigDecimal avg = sum.divide(BigDecimal.valueOf(rows.size()), 2, java.math.RoundingMode.HALF_UP);
            String bestName = nameCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("");
            String bestPublisher = pubCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("");
            String covered = agreements.isEmpty() ? "" : String.join(",", agreements);

            JournalApcPrice price = new JournalApcPrice();
            price.setIssn(entry.getKey());
            price.setJournalName(bestName);
            price.setPublisher(bestPublisher);
            price.setApcEur(avg);
            price.setApcSource("openapc_avg");
            price.setCoveredByAgreements(covered);
            prices.add(price);
        }

        repository.saveAll(prices);

        double durationSec = (System.currentTimeMillis() - start) / 1000.0;
        return new ApcImportResult("OK", totalRows, grouped.size(), prices.size(), skippedErrors, durationSec);
    }

    private String getCol(String[] line, Map<String, Integer> colIndex, String colName) {
        Integer idx = colIndex.get(colName);
        if (idx == null || idx >= line.length) return "";
        String val = line[idx];
        return val != null ? val.trim() : "";
    }

    private record CsvRow(String journalName, String publisher, BigDecimal euro, String agreement) {}
}
