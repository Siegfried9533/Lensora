package com.example.backend.dto;

public record AuthResponse(
        String token,
        String userId,
        String username,
        String email,
        String role) {
}
