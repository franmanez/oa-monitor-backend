package es.oamonitor.dto;

import java.util.Map;

public record InstitutionSummaryDto(
    String institutionId,
    String institutionName,
    long totalPublications,
    Map<String, Long> oaStatusCounts,
    double oaPercentage,
    long foundInRepository,
    Map<Integer, Map<String, Long>> yearlyBreakdown
) {}
