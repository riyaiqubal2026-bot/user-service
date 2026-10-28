package com.shawbin.user_service.service;

import com.shawbin.user_service.entity.User;
import com.shawbin.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    public User createUser(User user){
        if(userRepository.existsByEmail(user.getEmail())){
            throw new IllegalArgumentException("Email already Exists");
        }
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
}
