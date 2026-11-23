package es.oamonitor.dto;

import java.math.BigDecimal;

public record PublisherArticleDto(
    String id,
    String doi,
    String title,
    String journalName,
    String journalIssn,
    BigDecimal apcEstimatedEur,
    String oaStatus
) {}
