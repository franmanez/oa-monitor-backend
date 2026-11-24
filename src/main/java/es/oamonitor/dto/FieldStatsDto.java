package es.oamonitor.dto;

import java.math.BigDecimal;

public record FieldStatsDto(
    String field,
    long articleCount,
    double sharePct,
    long diamondCount,
    long goldCount,
    long greenCount,
    long hybridCount,
    long bronzeCount,
    long closedCount,
    double broadOaPct,
    double strongOaPct,
    long repositoryCount,
    double repositoryPct,
    BigDecimal totalApc,
    long articlesWithApc,
    BigDecimal averageApc
) {}
