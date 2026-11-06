package es.oamonitor.dto;

import java.util.List;

public record HistoricalDataDto(
    String institutionId,
    String institutionName,
    List<HistoricalYearDto> yearlyData
) {}
