package es.oamonitor.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateInstitutionRequest(
    @NotBlank String name,
    @NotBlank String rorId,
    String oaiEndpoint,
    String oaiMetadataPrefix,
    String rebiunCode
) {}
