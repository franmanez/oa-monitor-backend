package es.oamonitor.dto;

import java.math.BigDecimal;

public record ApcYearBreakdown(
    int year,
    BigDecimal totalCost,
    int publicationsCount,
    int publicationsWithPrice
) {}
