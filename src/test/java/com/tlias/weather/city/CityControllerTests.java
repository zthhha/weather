package com.tlias.weather.city;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CityControllerTests {
    @Autowired MockMvc mockMvc;
    @Autowired CityRepository cities;

    @Test
    void listsCitiesWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void createsCityWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Beijing\",\"locationId\":\"101010100\",\"timezone\":\"Asia/Shanghai\",\"enabled\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Beijing"))
                .andExpect(jsonPath("$.data.locationId").value("101010100"));
    }

    @Test
    void updatesCityWithoutAuthentication() throws Exception {
        City city = cities.save(new City("Shanghai", "101020100", "Asia/Shanghai", true));

        mockMvc.perform(put("/api/v1/cities/" + city.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Shanghai Updated\",\"locationId\":\"101020100\",\"timezone\":\"Asia/Shanghai\",\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Shanghai Updated"))
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    void loginEndpointIsGone() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"password\"}"))
                .andExpect(status().isNotFound());
    }
}
