package com.tlias.weather.forecast;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AqiForecastRepository extends JpaRepository<AqiForecast, Long> {
    List<AqiForecast> findByCityIdAndForecastAtBetweenOrderByForecastAtAsc(Long cityId, Instant from, Instant to);
    void deleteByCityIdAndModelVersion(Long cityId, String modelVersion);
}
