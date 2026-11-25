
package com.shawbin.user_service.service;

import com.shawbin.user_service.dto.RegisterRequest;
import com.shawbin.user_service.entity.User;
import com.shawbin.user_service.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Riya");
        user.setEmail("riya@example.com");
        user.setAge(25);

        registerRequest = new RegisterRequest();
        registerRequest.setName("Riya");
        registerRequest.setEmail("riya@example.com");
        registerRequest.setAge(25);
        registerRequest.setPassword("Test@123");
    }

    @Test
    void getAllUsersShouldReturnAllUsers() {
        User secondUser = new User();
        secondUser.setName("Iqbal");
        secondUser.setEmail("iqbal@example.com");
        secondUser.setAge(28);

        when(userRepository.findAll())
                .thenReturn(Arrays.asList(user, secondUser));

        assertEquals(2, userService.getAllUsers().size());
        assertEquals("Riya", userService.getAllUsers().get(0).getName());
    }

    @Test
    void getAllUsersShouldReturnEmptyList() {
        when(userRepository.findAll())
                .thenReturn(Collections.emptyList());

        assertTrue(userService.getAllUsers().isEmpty());
    }

    @Test
    void getUserByIdShouldReturnUserWhenFound() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("Riya", result.get().getName());
    }

    @Test
    void getUserByIdShouldReturnEmptyWhenNotFound() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertTrue(userService.getUserById(99L).isEmpty());
    }

    @Test
    void createUserShouldSaveNewUser() {
        when(userRepository.existsByEmail("riya@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("Test@123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(registerRequest);

        assertEquals("riya@example.com", result.getEmail());
        assertEquals("Riya", result.getName());
        assertEquals("encodedPassword", result.getPasswordHash());

        verify(passwordEncoder).encode("Test@123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserShouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail("riya@example.com"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(registerRequest)
        );

        assertEquals("Email already exists", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUserShouldDeleteUserById() {
        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void updateUserShouldUpdateExistingUser() {
        User updatedUser = new User();
        updatedUser.setName("Riya Updated");
        updatedUser.setEmail("riya.new@example.com");
        updatedUser.setAge(26);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("riya.new@example.com"))
                .thenReturn(false);

        when(userRepository.save(user)).thenReturn(user);

        Optional<User> result =
                userService.updateUser(1L, updatedUser);

        assertTrue(result.isPresent());
        assertEquals("Riya Updated", result.get().getName());
        assertEquals("riya.new@example.com", result.get().getEmail());
        assertEquals(26, result.get().getAge());

        verify(userRepository).save(user);
    }

    @Test
    void updateUserShouldAllowKeepingOwnEmail() {
        User updatedUser = new User();
        updatedUser.setName("Riya Updated");
        updatedUser.setEmail("riya@example.com");
        updatedUser.setAge(26);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user)).thenReturn(user);

        Optional<User> result =
                userService.updateUser(1L, updatedUser);

        assertTrue(result.isPresent());

        verify(userRepository, never())
                .existsByEmail("riya@example.com");
    }

    @Test
    void updateUserShouldRejectAnotherUsersEmail() {
        User updatedUser = new User();
        updatedUser.setName("Riya");
        updatedUser.setEmail("iqbal@example.com");
        updatedUser.setAge(25);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("iqbal@example.com"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(1L, updatedUser)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserShouldReturnEmptyWhenUserNotFound() {
        User updatedUser = new User();
        updatedUser.setName("Riya");
        updatedUser.setEmail("riya.new@example.com");
        updatedUser.setAge(26);

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<User> result =
                userService.updateUser(99L, updatedUser);

        assertTrue(result.isEmpty());

        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void loginShouldReturnUserWhenCredentialsAreValid() {

        when(userRepository.findByEmail("riya@example.com"))
                .thenReturn(Optional.of(user));

        user.setPasswordHash("encodedPassword");

        when(passwordEncoder.matches("Test@123", "encodedPassword"))
                .thenReturn(true);

        User result = userService.login("riya@example.com", "Test@123");

        assertNotNull(result);
        assertEquals("riya@example.com", result.getEmail());

        verify(passwordEncoder).matches("Test@123", "encodedPassword");
    }

    @Test
    void loginShouldThrowExceptionWhenEmailDoesNotExist() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.login("unknown@example.com", "Test@123")
        );

        assertEquals("Invalid email or password", exception.getMessage());

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void loginShouldThrowExceptionWhenPasswordIsIncorrect() {

        user.setPasswordHash("encodedPassword");

        when(userRepository.findByEmail("riya@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("WrongPassword", "encodedPassword"))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.login("riya@example.com", "WrongPassword")
        );

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void loginShouldUseEmailToFindUser() {

        when(userRepository.findByEmail("riya@example.com"))
                .thenReturn(Optional.of(user));

        user.setPasswordHash("encodedPassword");

        when(passwordEncoder.matches("Test@123", "encodedPassword"))
                .thenReturn(true);

        userService.login("riya@example.com", "Test@123");

        verify(userRepository).findByEmail("riya@example.com");
    }

}
