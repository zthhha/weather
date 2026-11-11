package com.tlias.weather.security;

import com.tlias.weather.security.api.AuthenticationController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerTests {
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void logsInWithValidCredentials() throws Exception {
        Role role = roles.findByCode("ROLE_ADMIN").orElseGet(() -> roles.save(new Role("ROLE_ADMIN", "Administrator")));
        users.save(new User("admin", passwordEncoder.encode("correct-password"), java.util.Set.of(role)));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"correct-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    void rejectsInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"missing\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }
}
