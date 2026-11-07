package com.shawbin.user_service.controller;

import com.shawbin.user_service.dto.LoginRequest;
import com.shawbin.user_service.dto.LoginResponse;
import com.shawbin.user_service.dto.RegisterRequest;
import com.shawbin.user_service.dto.UserResponse;
import com.shawbin.user_service.entity.User;
import com.shawbin.user_service.service.JwtService;
import com.shawbin.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService,JwtService jwtService){
        this.userService=userService;
        this.jwtService=jwtService;
    }

    @PostMapping("/saveUser")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponse> saveUser(
            @Valid @RequestBody RegisterRequest request) {

        User savedUser = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toUserResponse(savedUser));
    }

    @GetMapping("/fetchAllUsers")
    public ResponseEntity<List<UserResponse>> fetchAllUsers() {

        List<UserResponse> users = userService.getAllUsers()
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/fetchUser/{id}")
    public ResponseEntity<UserResponse> fetchUserById(@PathVariable Long id) {

        return userService.getUserById(id)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("updateUser/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User user) {

        return userService.updateUser(id, user)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge()
        );
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        try {
            User user = userService.login(
                    request.getEmail(),
                    request.getPassword()
            );

            String token = jwtService.generateToken(
                    user.getId(),
                    user.getEmail()
            );

            LoginResponse response = new LoginResponse(
                    token,
                    user.getId(),
                    user.getName(),
                    user.getEmail()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }



}
