package com.tlias.weather.forecast;

import com.tlias.weather.airquality.AirQualityObservation;
import com.tlias.weather.airquality.AirQualityReading;
import com.tlias.weather.city.City;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LinearTrendForecastEngineTests {
    private final LinearTrendForecastEngine engine = new LinearTrendForecastEngine();
    private final City city = new City("Beijing", "101010100", "Asia/Shanghai", true);
    private final Instant generatedAt = Instant.parse("2026-11-11T10:35:00Z");

    @Test
    void producesTwentyFourOrderedPredictionsForStableData() {
        List<ForecastEngine.ForecastPoint> forecasts = engine.forecast(List.of(observation(80, 0), observation(80, 1), observation(80, 2)), generatedAt);

        assertThat(forecasts).hasSize(24);
        assertThat(forecasts.getFirst().forecastAt()).isEqualTo(Instant.parse("2026-11-11T11:00:00Z"));
        assertThat(forecasts).allMatch(point -> point.aqi() == 80);
        assertThat(forecasts).isSortedAccordingTo((left, right) -> left.forecastAt().compareTo(right.forecastAt()));
    }

    @Test
    void followsAnIncreasingTrend() {
        List<ForecastEngine.ForecastPoint> forecasts = engine.forecast(List.of(observation(50, 0), observation(60, 1), observation(70, 2)), generatedAt);

        assertThat(forecasts.getFirst().aqi()).isGreaterThan(60);
        assertThat(forecasts.getLast().aqi()).isGreaterThan(forecasts.getFirst().aqi());
    }

    @Test
    void producesTwentyFourFlatPredictionsForOneHistoricalObservation() {
        List<ForecastEngine.ForecastPoint> forecasts = engine.forecast(List.of(observation(80, 0)), generatedAt);

        assertThat(forecasts).hasSize(24);
        assertThat(forecasts).allMatch(point -> point.aqi() == 80);
    }

    @Test
    void producesTwentyFourZeroPredictionsWithoutHistoricalObservations() {
        List<ForecastEngine.ForecastPoint> forecasts = engine.forecast(List.of(), generatedAt);

        assertThat(forecasts).hasSize(24);
        assertThat(forecasts).allMatch(point -> point.aqi() == 0);
    }

    @Test
    void constrainsPredictionsToValidAqiRange() {
        List<ForecastEngine.ForecastPoint> forecasts = engine.forecast(List.of(observation(450, 0), observation(480, 1), observation(500, 2)), generatedAt);

        assertThat(forecasts).allMatch(point -> point.aqi() >= 0 && point.aqi() <= 500);
    }

    private AirQualityObservation observation(int aqi, int hour) {
        Instant observedAt = Instant.parse("2026-11-11T00:00:00Z").plusSeconds(hour * 3600L);
        return new AirQualityObservation(city, new AirQualityReading(observedAt, "TEST", aqi,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE), observedAt);
    }
}
