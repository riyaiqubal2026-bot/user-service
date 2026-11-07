package com.shawbin.user_service.dto;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Integer age;

    public UserResponse(Long id, String name, String email, Integer age) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Integer getAge() {
        return age;
    }
}