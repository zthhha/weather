package com.tlias.weather.alert.api;

import com.tlias.weather.alert.AlertMetric;
import com.tlias.weather.alert.AlertOperator;
import com.tlias.weather.alert.AlertRule;

import java.math.BigDecimal;
import java.time.Instant;

public record AlertRuleResponse(
        Long id,
        String name,
        Long cityId,
        String cityName,
        boolean global,
        AlertMetric metric,
        AlertOperator operator,
        BigDecimal threshold,
        String severity,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    public static AlertRuleResponse from(AlertRule rule) {
        return new AlertRuleResponse(rule.getId(), rule.getName(),
                rule.getCity() == null ? null : rule.getCity().getId(),
                rule.getCity() == null ? null : rule.getCity().getName(),
                rule.getCity() == null, rule.getMetric(), rule.getOperator(), rule.getThreshold(), rule.getSeverity(),
                rule.isEnabled(), rule.getCreatedAt(), rule.getUpdatedAt());
    }
}
