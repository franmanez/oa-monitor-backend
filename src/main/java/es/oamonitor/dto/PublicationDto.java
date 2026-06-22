package es.oamonitor.dto;

import java.math.BigDecimal;

public record PublicationDto(
    String id,
    String doi,
    String title,
    Integer publicationYear,
    String journalIssn,
    String journalName,
    String oaStatusOpenalex,
    String oaStatusResolved,
    Boolean foundInRepository,
    String repositoryUrl,
    BigDecimal apcEstimatedEur,
    Boolean apcCoveredByAgreement
) {}
