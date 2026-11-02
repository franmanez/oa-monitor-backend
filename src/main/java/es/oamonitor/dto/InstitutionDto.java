package es.oamonitor.dto;

import es.oamonitor.model.enums.OaiStatus;
import java.time.LocalDateTime;

public record InstitutionDto(
    String id,
    String name,
    String rorId,
    String oaiEndpoint,
    String oaiMetadataPrefix,
    String rebiunCode,
    LocalDateTime lastHarvestAt,
    OaiStatus oaiStatus,
    String autonomousCommunity,
    long totalPublications
) {}
