package es.oamonitor.dto;

import java.math.BigDecimal;

public record InstitutionScatterDto(
    String institutionId,
    String institutionName,
    long totalPublications,
    double oaPercentage,
    BigDecimal totalApcCost,
    boolean hasApcData
) {}
