
package com.shawbin.user_service.config;

import com.shawbin.user_service.controller.UserController;
import com.shawbin.user_service.exception.GlobalExceptionHandler;
import com.shawbin.user_service.service.JwtService;
import com.shawbin.user_service.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:securitytestdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void passwordEncoderShouldEncodePassword() {
        String rawPassword = "Test@123";

        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void loginEndpointShouldBeAccessibleWithoutAuthentication() throws Exception {

        Map<String, String> request = Map.of(
                "email", "riya@example.com",
                "password", "WrongPassword"
        );

        when(userService.login("riya@example.com", "WrongPassword"))
                .thenThrow(new IllegalArgumentException(
                        "Invalid email or password"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(userService).login("riya@example.com", "WrongPassword");
    }

    @Test
    void protectedEndpointShouldRejectUnauthenticatedRequest() throws Exception {

        mockMvc.perform(get("/api/users/fetchAllUsers"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void corsShouldAllowFrontendOrigin() throws Exception {

        mockMvc.perform(options("/api/users/login")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers",
                                "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        "http://localhost:4200"));
    }
}
