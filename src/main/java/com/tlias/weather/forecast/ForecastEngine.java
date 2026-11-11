package com.tlias.weather.forecast;

import com.tlias.weather.airquality.AirQualityObservation;

import java.time.Instant;
import java.util.List;

public interface ForecastEngine {
    List<ForecastPoint> forecast(List<AirQualityObservation> observations, Instant generatedAt);

    record ForecastPoint(Instant forecastAt, int aqi) {
    }
}
