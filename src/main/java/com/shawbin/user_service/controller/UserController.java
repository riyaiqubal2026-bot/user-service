package com.shawbin.user_service.controller;

import com.shawbin.user_service.entity.User;
import com.shawbin.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService=userService;
    }

    @PostMapping("/saveUser")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<User> saveUser(@Valid @RequestBody User user){
        User savedUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping("/fetchAllUsers")
    public ResponseEntity<List<User>> fetchAllUsers(){
        List<User> users=userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/fetchUser/{id}")
    public ResponseEntity<User> fetchUserById(@PathVariable Long id){
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("updateUser/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id , @Valid @RequestBody User user){
        return userService.updateUser(id,user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



}
