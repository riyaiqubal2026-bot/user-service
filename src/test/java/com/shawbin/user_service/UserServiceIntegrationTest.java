
package com.shawbin.user_service;

import com.shawbin.user_service.dto.RegisterRequest;
import com.shawbin.user_service.entity.User;
import com.shawbin.user_service.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:userintegrationdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "JWT_SECRET=VGhpc0lzQVNlY3VyZVRlc3RTZWNyZXRLZXlGb3JIU01BVEVTdGluZ09ubHk=",
        "JWT_EXPIRATION_MS=3600000"
})
@AutoConfigureMockMvc
class UserServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerUserShouldSaveUserAndHashPassword() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setName("Integration User");
        request.setEmail("integration@example.com");
        request.setAge(25);
        request.setPassword("Test@123");

        mockMvc.perform(post("/api/users/saveUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration User"))
                .andExpect(jsonPath("$.email").value("integration@example.com"));

        User savedUser = userRepository.findByEmail("integration@example.com")
                .orElseThrow();

        assertNotNull(savedUser.getId());
        assertTrue(passwordEncoder.matches(
                "Test@123",
                savedUser.getPasswordHash()
        ));
        assertNotEquals("Test@123", savedUser.getPasswordHash());
    }

    @Test
    void loginShouldReturnJwtTokenForRegisteredUser() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setName("Login User");
        request.setEmail("login@example.com");
        request.setAge(26);
        request.setPassword("Test@123");

        mockMvc.perform(post("/api/users/saveUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Map<String, String> loginRequest = Map.of(
                "email", "login@example.com",
                "password", "Test@123"
        );

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.name").value("Login User"))
                .andExpect(jsonPath("$.email").value("login@example.com"));
    }

    @Test
    void loginShouldRejectIncorrectPassword() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setName("Login User");
        request.setEmail("wrongpassword@example.com");
        request.setAge(26);
        request.setPassword("Test@123");

        mockMvc.perform(post("/api/users/saveUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Map<String, String> loginRequest = Map.of(
                "email", "wrongpassword@example.com",
                "password", "WrongPassword"
        );

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerUserShouldRejectInvalidAge() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setName("Underage User");
        request.setEmail("underage@example.com");
        request.setAge(17);
        request.setPassword("Test@123");

        mockMvc.perform(post("/api/users/saveUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertFalse(userRepository.existsByEmail("underage@example.com"));
    }

    @Test
    void registerUserShouldRejectDuplicateEmail() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setName("First User");
        request.setEmail("duplicate@example.com");
        request.setAge(25);
        request.setPassword("Test@123");

        mockMvc.perform(post("/api/users/saveUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        RegisterRequest duplicateRequest = new RegisterRequest();
        duplicateRequest.setName("Second User");
        duplicateRequest.setEmail("duplicate@example.com");
        duplicateRequest.setAge(28);
        duplicateRequest.setPassword("Another@123");

        mockMvc.perform(post("/api/users/saveUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());

        assertEquals(1, userRepository.count());
    }
}
