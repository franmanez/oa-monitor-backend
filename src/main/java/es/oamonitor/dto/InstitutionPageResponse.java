package es.oamonitor.dto;

import java.util.List;

public record InstitutionPageResponse(
    List<InstitutionDto> content,
    long totalElements,
    int totalPages,
    int number,
    int size,
    boolean first,
    boolean last
) {}
