package es.oamonitor.dto;

public record RegionalStatsDto(
    String community,
    int universityCount,
    long totalPublications,
    double oaPercentage,
    double strongOaPct,
    double repositoryPct,
    double diamondPct,
    double goldPct,
    double greenPct,
    double hybridPct,
    double bronzePct,
    double closedPct,
    double complianceScore,
    String complianceGrade
) {}
