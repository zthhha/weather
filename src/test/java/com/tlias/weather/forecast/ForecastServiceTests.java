package com.tlias.weather.forecast;

import com.tlias.weather.airquality.AirQualityObservation;
import com.tlias.weather.airquality.AirQualityObservationRepository;
import com.tlias.weather.airquality.AirQualityReading;
import com.tlias.weather.city.City;
import com.tlias.weather.city.CityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ForecastServiceTests {
    @Autowired ForecastService service;
    @Autowired CityRepository cities;
    @Autowired AirQualityObservationRepository observations;
    @Autowired AqiForecastRepository forecasts;

    @Test
    void persistsTwentyFourForecastsWhenEnoughObservationsExist() {
        City city = cities.save(new City("Beijing", "101010100", "Asia/Shanghai", true));
        Instant base = Instant.now().minusSeconds(3 * 3600L);
        for (int i = 0; i < 3; i++) {
            Instant observedAt = base.plusSeconds(i * 3600L);
            observations.save(new AirQualityObservation(city,
                    new AirQualityReading(observedAt, "TEST", 70 + i * 10, BigDecimal.ONE, BigDecimal.ONE,
                            BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE), observedAt));
        }

        ForecastService.ForecastGenerationResult result = service.regenerate(city.getId());

        assertThat(result.qualityStatus()).isEqualTo(ForecastService.READY);
        assertThat(result.pointsGenerated()).isEqualTo(24);
        assertThat(forecasts.findAll()).hasSize(24);
    }

    @Test
    void createsTwentyFourZeroForecastsWithoutHistoricalObservations() {
        City city = cities.save(new City("Shanghai", "101020100", "Asia/Shanghai", true));

        ForecastService.ForecastGenerationResult result = service.regenerate(city.getId());

        assertThat(result.qualityStatus()).isEqualTo(ForecastService.READY);
        assertThat(result.pointsGenerated()).isEqualTo(24);
        assertThat(forecasts.findAll()).hasSize(24);
        assertThat(forecasts.findAll()).allMatch(forecast -> forecast.getAqi() == 0);
    }
}
