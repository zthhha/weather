package com.tlias.weather.airquality;

import java.math.BigDecimal;
import java.time.Instant;

public record AirQualityReading(
        Instant observedAt,
        String source,
        int aqi,
        BigDecimal pm25,
        BigDecimal pm10,
        BigDecimal so2,
        BigDecimal no2,
        BigDecimal co,
        BigDecimal o3
) {
}
