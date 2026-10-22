package es.oamonitor.dto;

import es.oamonitor.model.enums.HarvestStatus;
import es.oamonitor.model.enums.HarvestType;
import java.time.LocalDateTime;

public record HarvestJobDto(
    String id,
    String institutionCode,
    HarvestType harvestType,
    HarvestStatus status,
    int recordsFetched,
    int recordsUpdated,
    String errorMessage,
    LocalDateTime startedAt,
    LocalDateTime finishedAt
) {}
