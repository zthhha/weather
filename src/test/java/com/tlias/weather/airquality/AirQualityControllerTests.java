package com.tlias.weather.airquality;

import com.tlias.weather.city.City;
import com.tlias.weather.city.CityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@WithMockUser
class AirQualityControllerTests {
    @Autowired MockMvc mockMvc;
    @Autowired CityRepository cities;
    @Autowired AirQualityObservationRepository observations;

    @Test
    void returnsLatestObservationForCity() throws Exception {
        City city = cities.save(new City("Beijing", "101010100", "Asia/Shanghai", true));
        observations.save(new AirQualityObservation(city,
                new AirQualityReading(Instant.parse("2026-11-11T00:00:00Z"), "QWEATHER", 120,
                        new BigDecimal("40"), new BigDecimal("60"), new BigDecimal("5"),
                        new BigDecimal("25"), new BigDecimal("0.9"), new BigDecimal("80")),
                Instant.parse("2026-11-11T00:01:00Z")));

        mockMvc.perform(get("/api/v1/air-quality/cities/" + city.getId() + "/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.aqi").value(120))
                .andExpect(jsonPath("$.data.aqiLevel").value("轻度污染"))
                .andExpect(jsonPath("$.data.cityName").value("Beijing"));
    }

    @Test
    void returnsNotFoundWhenNoObservation() throws Exception {
        City city = cities.save(new City("Shanghai", "101020100", "Asia/Shanghai", true));

        mockMvc.perform(get("/api/v1/air-quality/cities/" + city.getId() + "/latest"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsHistoryWithinRange() throws Exception {
        City city = cities.save(new City("Guangzhou", "101280100", "Asia/Shanghai", true));
        Instant base = Instant.parse("2026-11-11T00:00:00Z");
        for (int i = 0; i < 3; i++) {
            observations.save(new AirQualityObservation(city,
                    new AirQualityReading(base.plusSeconds(i * 3600L), "QWEATHER", 50 + i * 10, null, null, null, null, null, null),
                    base.plusSeconds(i * 3600L + 60)));
        }

        mockMvc.perform(get("/api/v1/air-quality/cities/" + city.getId() + "/history")
                        .param("from", "2026-11-11T00:00:00Z")
                        .param("to", "2026-11-11T03:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    void ranksCitiesByLatestAqi() throws Exception {
        City beijing = cities.save(new City("Beijing", "101010100", "Asia/Shanghai", true));
        City haikou = cities.save(new City("Haikou", "101310101", "Asia/Shanghai", true));
        Instant observedAt = Instant.parse("2026-11-11T00:00:00Z");
        observations.save(new AirQualityObservation(beijing, new AirQualityReading(observedAt, "TEST", 80, null, null, null, null, null, null), observedAt));
        observations.save(new AirQualityObservation(haikou, new AirQualityReading(observedAt, "TEST", 120, null, null, null, null, null, null), observedAt));

        mockMvc.perform(get("/api/v1/air-quality/rankings").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].cityName").value("Haikou"))
                .andExpect(jsonPath("$.data[0].aqi").value(120));
    }

    @Test
    void rejectsInvalidRankingLimit() throws Exception {
        mockMvc.perform(get("/api/v1/air-quality/rankings").param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsRangeLargerThanSevenDays() throws Exception {
        City city = cities.save(new City("Chengdu", "101270100", "Asia/Shanghai", true));

        mockMvc.perform(get("/api/v1/air-quality/cities/" + city.getId() + "/history")
                        .param("from", "2026-11-01T00:00:00Z")
                        .param("to", "2026-11-11T00:00:00Z"))
                .andExpect(status().isBadRequest());
    }
}
