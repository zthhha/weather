package com.tlias.weather.forecast;

import com.tlias.weather.airquality.AirQualityObservation;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class LinearTrendForecastEngine implements ForecastEngine {
    static final int REQUIRED_SAMPLES = 3;
    static final int FORECAST_HOURS = 24;

    @Override
    public List<ForecastPoint> forecast(List<AirQualityObservation> observations, Instant generatedAt) {
        if (observations.size() < REQUIRED_SAMPLES) {
            return List.of();
        }
        List<AirQualityObservation> samples = observations.stream()
                .sorted((left, right) -> left.getObservedAt().compareTo(right.getObservedAt()))
                .toList();
        double average = samples.stream().mapToInt(AirQualityObservation::getAqi).average().orElseThrow();
        double slopePerSample = (samples.getLast().getAqi() - samples.getFirst().getAqi()) / (double) (samples.size() - 1);
        Instant firstForecastAt = generatedAt.truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS);
        return IntStream.rangeClosed(1, FORECAST_HOURS)
                .mapToObj(hour -> new ForecastPoint(firstForecastAt.plus(hour - 1L, ChronoUnit.HOURS),
                        clamp(Math.round((float) (average + slopePerSample * hour)))))
                .toList();
    }

    private int clamp(int aqi) {
        return Math.max(0, Math.min(500, aqi));
    }
}
