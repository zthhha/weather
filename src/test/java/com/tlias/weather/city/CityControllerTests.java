package com.tlias.weather.city;

import com.tlias.weather.airquality.AirQualityProviderException;
import com.tlias.weather.city.api.CityController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@ContextConfiguration(classes = CityControllerTests.TestConfig.class)
@Transactional
@WithMockUser(roles = "ADMIN")
class CityControllerTests {
    @Autowired MockMvc mockMvc;

    @Test
    void createsCityByResolvingItsName() throws Exception {
        mockMvc.perform(post("/api/v1/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"海口\",\"enabled\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("海口"))
                .andExpect(jsonPath("$.data.locationId").value("101310101"))
                .andExpect(jsonPath("$.data.timezone").value("Asia/Shanghai"));
    }

    @Test
    void reportsBadGatewayWhenCityCannotBeResolved() throws Exception {
        mockMvc.perform(post("/api/v1/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Unknown City\",\"enabled\":true}"))
                .andExpect(status().is5xxServerError());
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        CityLocationResolver testCityLocationResolver() {
            return name -> {
                if ("Unknown City".equals(name)) throw new AirQualityProviderException("City not found in QWeather: " + name);
                return new ResolvedCity("101310101", "Asia/Shanghai");
            };
        }
    }
}
