package es.oamonitor.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "journal_apc_price")
@IdClass(JournalApcPriceId.class)
public class JournalApcPrice {

    @Id
    @Column(length = 20)
    private String issn;

    @Id
    @Column
    private Integer year;

    @Column(name = "journal_name")
    private String journalName;

    @Column
    private String publisher;

    @Column(name = "apc_eur", precision = 10, scale = 2)
    private BigDecimal apcEur;

    @Column(name = "apc_source")
    private String apcSource;

    public String getIssn() { return issn; }
    public void setIssn(String issn) { this.issn = issn; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getJournalName() { return journalName; }
    public void setJournalName(String journalName) { this.journalName = journalName; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public BigDecimal getApcEur() { return apcEur; }
    public void setApcEur(BigDecimal apcEur) { this.apcEur = apcEur; }

    public String getApcSource() { return apcSource; }
    public void setApcSource(String apcSource) { this.apcSource = apcSource; }
}
