package com.shawbin.user_service.service;

import com.shawbin.user_service.dto.RegisterRequest;
import com.shawbin.user_service.dto.UpdateUserRequest;
import com.shawbin.user_service.entity.User;
import com.shawbin.user_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;

    }

    public User createUser(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());

        // Hash the password before saving
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        return userRepository.save(user);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id){
        return userRepository.findById(id);
    }

    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }

    public Optional<User> updateUser(Long id, User updatedUser) {

        return userRepository.findById(id)
                .map(existingUser -> {
                    if(!existingUser.getEmail().equals(updatedUser.getEmail())
                            && userRepository.existsByEmail(updatedUser.getEmail())){
                        throw new IllegalArgumentException("Email already exists");
                    }
                    existingUser.setName(updatedUser.getName());
                    existingUser.setEmail(updatedUser.getEmail());
                    existingUser.setAge(updatedUser.getAge());

                    return userRepository.save(existingUser);
                });
    }

    public User login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return user;
    }

    public Optional<User> updateCurrentUser(
            Long id,
            UpdateUserRequest request) {

        return userRepository.findById(id).map(existingUser -> {

            if (!existingUser.getEmail().equals(request.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }

            existingUser.setName(request.getName());
            existingUser.setEmail(request.getEmail());
            existingUser.setAge(request.getAge());

            return userRepository.save(existingUser);
        });
    }
}
