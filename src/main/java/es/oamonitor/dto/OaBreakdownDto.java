package es.oamonitor.dto;

import java.util.Map;

public record OaBreakdownDto(
    String institutionId,
    String institutionName,
    Map<Integer, Map<String, Long>> yearlyStatusCounts,
    Map<Integer, Double> yearlyOaPercentage
) {}
