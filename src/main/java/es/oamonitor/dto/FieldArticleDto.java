package es.oamonitor.dto;

import java.math.BigDecimal;

public record FieldArticleDto(
    String id,
    String doi,
    String title,
    String journalName,
    Integer publicationYear,
    String subfield,
    String topic,
    String oaStatus,
    BigDecimal apcEstimatedEur
) {}
