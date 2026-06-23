package es.oamonitor.dto;

import java.math.BigDecimal;

public record JournalApcDto(
    String journalName,
    String journalIssn,
    long publicationCount,
    BigDecimal totalApcCost
) {}
