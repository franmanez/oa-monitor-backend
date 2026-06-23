package es.oamonitor.dto;

public record JournalCountDto(
    String journalName,
    String journalIssn,
    long publicationCount
) {}
