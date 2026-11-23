package es.oamonitor.dto;

import java.math.BigDecimal;

public record PublisherApcDto(
    String publisher,
    BigDecimal totalApc,
    long articleCount,
    BigDecimal averageApc,
    BigDecimal stdDev,
    double percentage,
    long goldCount,
    long hybridCount
) {}
