package com.tlias.weather.alert.api;

import com.tlias.weather.alert.AlertMetric;
import com.tlias.weather.alert.AlertOperator;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AlertRuleRequest(
        @NotBlank String name,
        Long cityId,
        @NotNull AlertMetric metric,
        @NotNull AlertOperator operator,
        @NotNull @DecimalMin("0") BigDecimal threshold,
        @NotBlank String severity,
        boolean enabled
) {
}
