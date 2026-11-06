package es.oamonitor.dto;

import java.util.List;

public record HistoricalYearDto(
    Integer year,
    long totalPublications,
    long diamondCount,
    long goldCount,
    long greenCount,
    long hybridCount,
    long bronzeCount,
    long closedCount,
    long repositoryCount,
    double oaPercentage,
    double strongOaPct,
    double repositoryPct,
    double broadOaPct,
    double complianceScore,
    String complianceGrade,
    double totalApcCost,
    int articlesWithApc,
    double averageApc
) {}
