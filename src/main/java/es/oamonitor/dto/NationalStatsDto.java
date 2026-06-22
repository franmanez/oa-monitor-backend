package es.oamonitor.dto;

import java.util.Map;

public record NationalStatsDto(
    long totalPublications,
    long totalInstitutions,
    double overallOaPercentage,
    long totalOpenAccess,
    long totalClosed,
    Map<String, Long> oaStatusCounts
) {}
