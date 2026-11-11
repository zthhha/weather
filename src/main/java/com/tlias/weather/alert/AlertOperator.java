package com.tlias.weather.alert;

import java.math.BigDecimal;

public enum AlertOperator {
    GT { public boolean matches(BigDecimal value, BigDecimal threshold) { return value.compareTo(threshold) > 0; } },
    GTE { public boolean matches(BigDecimal value, BigDecimal threshold) { return value.compareTo(threshold) >= 0; } },
    LT { public boolean matches(BigDecimal value, BigDecimal threshold) { return value.compareTo(threshold) < 0; } },
    LTE { public boolean matches(BigDecimal value, BigDecimal threshold) { return value.compareTo(threshold) <= 0; } };

    public abstract boolean matches(BigDecimal value, BigDecimal threshold);
}
