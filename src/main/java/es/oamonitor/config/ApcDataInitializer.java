package es.oamonitor.config;

import es.oamonitor.model.JournalApcPrice;
import es.oamonitor.repository.JournalApcPriceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ApcDataInitializer implements CommandLineRunner {

    private final JournalApcPriceRepository repository;

    public ApcDataInitializer(JournalApcPriceRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;

        List<JournalApcPrice> prices = List.of(
            create("2041-1723", "Nature Communications", "Springer Nature", new BigDecimal("5390"), "openapc_avg", "CRUE-SPRINGER"),
            create("2045-2322", "Scientific Reports", "Springer Nature", new BigDecimal("2190"), "openapc_avg", "CRUE-SPRINGER"),
            create("1932-6203", "PLOS ONE", "Public Library of Science", new BigDecimal("1931"), "openapc_avg", ""),
            create("1664-1078", "Frontiers in Psychology", "Frontiers Media", new BigDecimal("2950"), "openapc_avg", ""),
            create("2071-1050", "Sustainability", "MDPI", new BigDecimal("2400"), "openapc_avg", ""),
            create("1664-8021", "Frontiers in Genetics", "Frontiers Media", new BigDecimal("3050"), "openapc_avg", ""),
            create("1424-8220", "Sensors", "MDPI", new BigDecimal("2600"), "openapc_avg", ""),
            create("2072-6694", "Cancers", "MDPI", new BigDecimal("2900"), "openapc_avg", ""),
            create("2073-4409", "Cells", "MDPI", new BigDecimal("2900"), "openapc_avg", ""),
            create("2075-1729", "Life", "MDPI", new BigDecimal("2600"), "openapc_avg", ""),
            create("2076-3417", "Applied Sciences", "MDPI", new BigDecimal("2400"), "openapc_avg", ""),
            create("2079-4991", "Nanomaterials", "MDPI", new BigDecimal("2900"), "openapc_avg", ""),
            create("2218-1989", "Metabolites", "MDPI", new BigDecimal("2400"), "openapc_avg", ""),
            create("2296-889X", "Frontiers in Molecular Biosciences", "Frontiers Media", new BigDecimal("2950"), "openapc_avg", ""),
            create("2296-701X", "Frontiers in Ecology and Evolution", "Frontiers Media", new BigDecimal("2500"), "openapc_avg", ""),
            create("1420-3049", "Molecules", "MDPI", new BigDecimal("2900"), "openapc_avg", ""),
            create("1999-4915", "Viruses", "MDPI", new BigDecimal("2600"), "openapc_avg", ""),
            create("2073-4425", "Genes", "MDPI", new BigDecimal("2600"), "openapc_avg", ""),
            create("0070-217X", "Current Topics in Microbiology and Immunology", "Springer Nature", new BigDecimal("2190"), "openapc_avg", "CRUE-SPRINGER"),
            create("1660-4601", "International Journal of Environmental Research and Public Health", "MDPI", new BigDecimal("2900"), "openapc_avg", "")
        );

        repository.saveAll(prices);
        System.out.println("APC seed: " + prices.size() + " journal prices inserted");
    }

    private JournalApcPrice create(String issn, String journalName, String publisher, 
                                    BigDecimal apcEur, String source, String agreements) {
        JournalApcPrice price = new JournalApcPrice();
        price.setIssn(issn);
        price.setJournalName(journalName);
        price.setPublisher(publisher);
        price.setApcEur(apcEur);
        price.setApcSource(source);
        price.setCoveredByAgreements(agreements);
        return price;
    }
}
