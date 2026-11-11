package com.tlias.weather.airquality;

import com.tlias.weather.city.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface AirQualityObservationRepository extends JpaRepository<AirQualityObservation, Long> {
    Optional<AirQualityObservation> findByCityAndObservedAtAndSource(City city, Instant observedAt, String source);
    Optional<AirQualityObservation> findFirstByCityIdOrderByObservedAtDesc(Long cityId);
}
