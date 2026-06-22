package es.oamonitor.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "journal_apc_price")
public class JournalApcPrice {

    @Id
    @Column(length = 20)
    private String issn;

    @Column(name = "journal_name")
    private String journalName;

    @Column
    private String publisher;

    @Column(name = "apc_eur", precision = 10, scale = 2)
    private BigDecimal apcEur;

    @Column(name = "apc_source")
    private String apcSource;

    @Column(name = "covered_by_agreements", columnDefinition = "TEXT")
    private String coveredByAgreements;

    public String getIssn() { return issn; }
    public void setIssn(String issn) { this.issn = issn; }

    public String getJournalName() { return journalName; }
    public void setJournalName(String journalName) { this.journalName = journalName; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public BigDecimal getApcEur() { return apcEur; }
    public void setApcEur(BigDecimal apcEur) { this.apcEur = apcEur; }

    public String getApcSource() { return apcSource; }
    public void setApcSource(String apcSource) { this.apcSource = apcSource; }

    public String getCoveredByAgreements() { return coveredByAgreements; }
    public void setCoveredByAgreements(String coveredByAgreements) { this.coveredByAgreements = coveredByAgreements; }
}
