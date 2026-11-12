package com.tlias.weather.alert;

import com.tlias.weather.city.City;
import com.tlias.weather.city.CityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
class AlertControllerTests {
    @Autowired MockMvc mockMvc;
    @Autowired CityRepository cities;

    @Test
    void createsAndListsCityRuleWithoutAuthentication() throws Exception {
        City city = cities.save(new City("Beijing", "101010100", "Asia/Shanghai", true));

        mockMvc.perform(post("/api/v1/alert-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"High AQI\",\"cityId\":" + city.getId()
                                + ",\"metric\":\"AQI\",\"operator\":\"GTE\",\"threshold\":150,\"severity\":\"HIGH\",\"enabled\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("High AQI"))
                .andExpect(jsonPath("$.data.cityId").value(city.getId()))
                .andExpect(jsonPath("$.data.global").value(false));

        mockMvc.perform(get("/api/v1/alert-rules").param("cityId", city.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void rejectsRuleForMissingCity() throws Exception {
        mockMvc.perform(post("/api/v1/alert-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"High AQI\",\"cityId\":9999,\"metric\":\"AQI\",\"operator\":\"GTE\",\"threshold\":150,\"severity\":\"HIGH\",\"enabled\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsNegativeThreshold() throws Exception {
        mockMvc.perform(post("/api/v1/alert-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Invalid\",\"metric\":\"AQI\",\"operator\":\"GTE\",\"threshold\":-1,\"severity\":\"HIGH\",\"enabled\":true}"))
                .andExpect(status().isBadRequest());
    }
}
