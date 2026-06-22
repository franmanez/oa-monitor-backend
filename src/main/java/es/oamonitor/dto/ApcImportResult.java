package es.oamonitor.dto;

public record ApcImportResult(
    String status,
    int totalRows,
    int uniqueIssns,
    int insertedOrUpdated,
    int skippedErrors,
    double durationSec
) {}
