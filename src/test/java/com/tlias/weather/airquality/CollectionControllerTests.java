package com.tlias.weather.airquality;

import com.tlias.weather.city.City;
import com.tlias.weather.city.CityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
class CollectionControllerTests {
    @Autowired MockMvc mockMvc;
    @Autowired CityRepository cities;
    @MockitoBean AirQualityProvider provider;

    @Test
    void startsManualCollectionAndReturnsJobSummary() throws Exception {
        City city = cities.save(new City("Beijing", "101010100", "Asia/Shanghai", true));
        when(provider.fetchCurrent(city)).thenReturn(new AirQualityReading(
                Instant.parse("2026-11-11T00:00:00Z"), "QWEATHER", 75,
                new BigDecimal("25"), new BigDecimal("45"), new BigDecimal("4"),
                new BigDecimal("20"), new BigDecimal("0.7"), new BigDecimal("80")));

        mockMvc.perform(post("/api/v1/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.totalCities").value(1))
                .andExpect(jsonPath("$.data.successfulCities").value(1))
                .andExpect(jsonPath("$.data.failedCities").value(0));
    }

    @Test
    void listsCollectionJobsWithMostRecentFirst() throws Exception {
        mockMvc.perform(get("/api/v1/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
