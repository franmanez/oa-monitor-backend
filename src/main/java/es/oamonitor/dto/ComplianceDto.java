package es.oamonitor.dto;

public record ComplianceDto(
    String institutionId,
    String institutionName,
    Integer year,
    long totalPublications,
    double strongOaPct,
    double repositoryPct,
    double broadOaPct,
    double overallScore,
    String grade,
    long strongOaCount,
    long repositoryCount,
    long broadOaCount
) {}
