package com.tlias.weather.airquality.api;

import com.tlias.weather.airquality.AirQualityObservation;
import com.tlias.weather.airquality.AqiCategory;

import java.math.BigDecimal;
import java.time.Instant;

public record AirQualityObservationResponse(
        Long id,
        Long cityId,
        String cityName,
        Instant observedAt,
        Instant collectedAt,
        String source,
        int aqi,
        String aqiLevel,
        String aqiColor,
        String healthAdvice,
        BigDecimal pm25,
        BigDecimal pm10,
        BigDecimal so2,
        BigDecimal no2,
        BigDecimal co,
        BigDecimal o3
) {
    public static AirQualityObservationResponse from(AirQualityObservation observation) {
        AqiCategory category = AqiCategory.from(observation.getAqi());
        return new AirQualityObservationResponse(
                observation.getId(),
                observation.getCity().getId(),
                observation.getCity().getName(),
                observation.getObservedAt(),
                observation.getCollectedAt(),
                observation.getSource(),
                observation.getAqi(),
                category.level(),
                category.color(),
                category.healthAdvice(),
                observation.getPm25(),
                observation.getPm10(),
                observation.getSo2(),
                observation.getNo2(),
                observation.getCo(),
                observation.getO3()
        );
    }
}
