package com.tlias.weather.city;

import com.tlias.weather.security.Role;
import com.tlias.weather.security.RoleRepository;
import com.tlias.weather.security.User;
import com.tlias.weather.security.UserRepository;
import com.tlias.weather.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CityControllerTests {
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        Role admin = roles.findByCode("ROLE_ADMIN").orElseGet(() -> roles.save(new Role("ROLE_ADMIN", "Administrator")));
        User user = users.findByUsername("city-admin").orElseGet(() ->
                users.save(new User("city-admin", passwordEncoder.encode("password"), Set.of(admin))));
        adminToken = jwtService.createToken(user);
    }

    @Test
    void requiresAuthenticationToListCities() throws Exception {
        mockMvc.perform(get("/api/v1/cities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void administratorCreatesCity() throws Exception {
        mockMvc.perform(post("/api/v1/cities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Beijing\",\"locationId\":\"101010100\",\"timezone\":\"Asia/Shanghai\",\"enabled\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Beijing"))
                .andExpect(jsonPath("$.data.locationId").value("101010100"));
    }
}
