
package com.shawbin.user_service.controller;

import com.shawbin.user_service.dto.RegisterRequest;
import com.shawbin.user_service.entity.User;
import com.shawbin.user_service.exception.GlobalExceptionHandler;
import com.shawbin.user_service.service.JwtService;
import com.shawbin.user_service.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private User createUser() {
        User user = new User();
        user.setName("Riya");
        user.setEmail("riya@example.com");
        user.setAge(25);
        return user;
    }

    private RegisterRequest createRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Riya");
        request.setEmail("riya@example.com");
        request.setAge(25);
        request.setPassword("Test@123");
        return request;
    }

    @Test
    void fetchAllUsersShouldReturnOk() throws Exception {
        when(userService.getAllUsers())
                .thenReturn(Arrays.asList(createUser()));

        mockMvc.perform(get("/api/users/fetchAllUsers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Riya"));
    }

    @Test
    void fetchAllUsersShouldReturnEmptyArray() throws Exception {
        when(userService.getAllUsers())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users/fetchAllUsers"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void fetchUserByIdShouldReturnUser() throws Exception {
        when(userService.getUserById(1L))
                .thenReturn(Optional.of(createUser()));

        mockMvc.perform(get("/api/users/fetchUser/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Riya"));
    }

    @Test
    void fetchUserByIdShouldReturnNotFound() throws Exception {
        when(userService.getUserById(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/fetchUser/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveUserShouldReturnCreated() throws Exception {
        RegisterRequest request = createRegisterRequest();
        User user = createUser();

        when(userService.createUser(any(RegisterRequest.class)))
                .thenReturn(user);

        mockMvc.perform(post("/api/users/saveUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email")
                        .value("riya@example.com"));
    }

    @Test
    void saveUserShouldReturnBadRequestForInvalidInput()
            throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("");
        request.setEmail("invalid-email");
        request.setAge(15);
        request.setPassword("");

        mockMvc.perform(post("/api/users/saveUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .createUser(any(RegisterRequest.class));
    }

    @Test
    void saveUserShouldReturnConflictForDuplicateEmail()
            throws Exception {
        RegisterRequest request = createRegisterRequest();

        when(userService.createUser(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException(
                        "Email already exists"));

        mockMvc.perform(post("/api/users/saveUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Email already exists"));
    }

    @Test
    void updateUserShouldReturnOk() throws Exception {
        User user = createUser();
        user.setName("Riya Updated");

        when(userService.updateUser(eq(1L), any(User.class)))
                .thenReturn(Optional.of(user));

        mockMvc.perform(put("/api/users/updateUser/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Riya Updated"));
    }

    @Test
    void updateUserShouldReturnNotFound() throws Exception {
        User user = createUser();

        when(userService.updateUser(eq(99L), any(User.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/updateUser/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserShouldReturnConflictForDuplicateEmail()
            throws Exception {
        User user = createUser();
        user.setEmail("iqbal@example.com");

        when(userService.updateUser(eq(1L), any(User.class)))
                .thenThrow(new IllegalArgumentException(
                        "Email already exists"));

        mockMvc.perform(put("/api/users/updateUser/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Email already exists"));
    }

    @Test
    void updateUserShouldReturnBadRequestForInvalidInput()
            throws Exception {
        User user = createUser();
        user.setName("");
        user.setEmail("invalid-email");
        user.setAge(15);

        mockMvc.perform(put("/api/users/updateUser/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .updateUser(eq(1L), any(User.class));
    }

    @Test
    void deleteUserShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/deleteUser/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }
}
