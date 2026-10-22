package es.oamonitor.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record ApcEstimateDto(
    UUID institutionId,
    String institutionName,
    BigDecimal totalEstimatedCost,
    int totalPublications,
    int publicationsWithPrice,
    int publicationsWithoutPrice,
    Map<Integer, ApcYearBreakdown> breakdownByYear
) {}
