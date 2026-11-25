
package com.shawbin.user_service.repository;

import com.shawbin.user_service.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User createUser() {
        User user = new User();
        user.setName("Riya");
        user.setEmail("riya@example.com");
        user.setAge(25);
        user.setPasswordHash("encoded-password");
        return user;
    }

    @Test
    void saveUserShouldPersistUser() {
        User savedUser = userRepository.save(createUser());

        assertNotNull(savedUser.getId());
        assertEquals("Riya", savedUser.getName());
        assertEquals("riya@example.com", savedUser.getEmail());
        assertEquals(25, savedUser.getAge());
    }

    @Test
    void findByEmailShouldReturnUserWhenEmailExists() {
        userRepository.save(createUser());

        Optional<User> result =
                userRepository.findByEmail("riya@example.com");

        assertTrue(result.isPresent());
        assertEquals("Riya", result.get().getName());
    }

    @Test
    void findByEmailShouldReturnEmptyWhenEmailDoesNotExist() {
        Optional<User> result =
                userRepository.findByEmail("unknown@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByEmailShouldReturnTrueWhenEmailExists() {
        userRepository.save(createUser());

        assertTrue(userRepository.existsByEmail("riya@example.com"));
    }

    @Test
    void existsByEmailShouldReturnFalseWhenEmailDoesNotExist() {
        assertFalse(userRepository.existsByEmail("unknown@example.com"));
    }

    @Test
    void deleteUserShouldRemoveUser() {
        User savedUser = userRepository.save(createUser());

        userRepository.deleteById(savedUser.getId());

        assertFalse(userRepository.findById(savedUser.getId()).isPresent());
    }

    @Test
    void findAllShouldReturnAllUsers() {
        userRepository.save(createUser());

        User secondUser = createUser();
        secondUser.setEmail("another@example.com");
        secondUser.setName("Alex");
        userRepository.save(secondUser);

        assertEquals(2, userRepository.findAll().size());
    }
}
