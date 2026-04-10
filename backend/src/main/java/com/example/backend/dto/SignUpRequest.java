package com.example.backend.dto;

public record SignUpRequest(
        String username,
        String email,
        String password) {

}
