package es.oamonitor.dto;

public record InstitutionRankingDto(
    String institutionId,
    String institutionName,
    long totalPublications,
    double oaPercentage,
    long openAccessCount,
    long closedCount
) {}
