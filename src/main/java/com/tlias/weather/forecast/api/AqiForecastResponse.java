package com.tlias.weather.forecast.api;

import com.tlias.weather.airquality.AqiCategory;
import com.tlias.weather.forecast.AqiForecast;

import java.time.Instant;

public record AqiForecastResponse(
        Instant forecastAt,
        Instant generatedAt,
        int aqi,
        String aqiLevel,
        String aqiColor,
        String healthAdvice,
        String modelVersion,
        String qualityStatus
) {
    public static AqiForecastResponse from(AqiForecast forecast) {
        AqiCategory category = AqiCategory.from(forecast.getAqi());
        return new AqiForecastResponse(forecast.getForecastAt(), forecast.getGeneratedAt(), forecast.getAqi(),
                category.level(), category.color(), category.healthAdvice(),
                forecast.getModelVersion(), forecast.getQualityStatus());
    }
}
