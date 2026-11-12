package com.tlias.weather.airquality.api;

import com.tlias.weather.airquality.AirQualityObservation;
import com.tlias.weather.airquality.AqiCategory;

import java.time.Instant;

public record CityRankingResponse(Long cityId, String cityName, int aqi, String aqiLevel, String aqiColor, Instant observedAt) {
    public static CityRankingResponse from(AirQualityObservation observation) {
        AqiCategory category = AqiCategory.from(observation.getAqi());
        return new CityRankingResponse(observation.getCity().getId(), observation.getCity().getName(), observation.getAqi(),
                category.level(), category.color(), observation.getObservedAt());
    }
}
