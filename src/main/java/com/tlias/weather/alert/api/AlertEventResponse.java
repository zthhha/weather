package com.tlias.weather.alert.api;

import com.tlias.weather.alert.AlertEvent;
import com.tlias.weather.alert.AlertEventStatus;
import com.tlias.weather.alert.AlertMetric;
import com.tlias.weather.alert.AlertOperator;

import java.math.BigDecimal;
import java.time.Instant;

public record AlertEventResponse(
        Long id,
        Long ruleId,
        String ruleName,
        Long cityId,
        String cityName,
        AlertMetric metric,
        AlertOperator operator,
        BigDecimal threshold,
        String severity,
        AlertEventStatus status,
        BigDecimal triggeredValue,
        BigDecimal lastEvaluatedValue,
        Instant firstTriggeredAt,
        Instant lastTriggeredAt,
        Instant resolvedAt
) {
    public static AlertEventResponse from(AlertEvent event) {
        return new AlertEventResponse(event.getId(), event.getRule().getId(), event.getRule().getName(),
                event.getCity().getId(), event.getCity().getName(), event.getRule().getMetric(),
                event.getRule().getOperator(), event.getRule().getThreshold(), event.getRule().getSeverity(),
                event.getStatus(), event.getTriggeredValue(), event.getLastEvaluatedValue(),
                event.getFirstTriggeredAt(), event.getLastTriggeredAt(), event.getResolvedAt());
    }
}
