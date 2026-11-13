package com.tlias.weather.forecast;

import com.tlias.weather.airquality.AirQualityObservation;
import com.tlias.weather.airquality.AirQualityObservationRepository;
import com.tlias.weather.city.City;
import com.tlias.weather.city.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ForecastService {
    public static final String MODEL_VERSION = "linear-trend-v1";
    public static final String READY = "READY";

    private final CityRepository cities;
    private final AirQualityObservationRepository observations;
    private final AqiForecastRepository forecasts;
    private final ForecastEngine engine;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public ForecastService(CityRepository cities, AirQualityObservationRepository observations,
                           AqiForecastRepository forecasts, ForecastEngine engine) {
        this(cities, observations, forecasts, engine, Clock.systemUTC());
    }

    ForecastService(CityRepository cities, AirQualityObservationRepository observations,
                    AqiForecastRepository forecasts, ForecastEngine engine, Clock clock) {
        this.cities = cities;
        this.observations = observations;
        this.forecasts = forecasts;
        this.engine = engine;
        this.clock = clock;
    }

    @Transactional
    public ForecastGenerationResult regenerate(Long cityId) {
        City city = cities.findById(cityId).orElseThrow(() -> new ForecastNotFoundException("City not found"));
        Instant generatedAt = clock.instant();
        List<AirQualityObservation> samples = observations.findByCityIdAndObservedAtBetweenOrderByObservedAtAsc(
                cityId, generatedAt.minus(7, ChronoUnit.DAYS), generatedAt);
        List<ForecastEngine.ForecastPoint> points = engine.forecast(samples, generatedAt);
        forecasts.deleteByCityIdAndModelVersion(cityId, MODEL_VERSION);
        forecasts.flush();
        if (points.isEmpty()) {
            return new ForecastGenerationResult(READY, 0);
        }
        List<AqiForecast> generated = points.stream()
                .map(point -> new AqiForecast(city, point.forecastAt(), generatedAt, point.aqi(), MODEL_VERSION, READY))
                .toList();
        forecasts.saveAll(generated);
        return new ForecastGenerationResult(READY, generated.size());
    }

    public record ForecastGenerationResult(String qualityStatus, int pointsGenerated) {
    }
}
