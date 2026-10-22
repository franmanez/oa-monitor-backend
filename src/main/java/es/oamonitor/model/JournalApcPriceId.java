package es.oamonitor.model;

import java.io.Serializable;
import java.util.Objects;

public class JournalApcPriceId implements Serializable {

    private String issn;
    private Integer year;

    public JournalApcPriceId() {}

    public JournalApcPriceId(String issn, Integer year) {
        this.issn = issn;
        this.year = year;
    }

    public String getIssn() { return issn; }
    public void setIssn(String issn) { this.issn = issn; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JournalApcPriceId that = (JournalApcPriceId) o;
        return Objects.equals(issn, that.issn) && Objects.equals(year, that.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issn, year);
    }
}
