package com.example.my_mobile_app.model;

/** Mirrors {@code AuthResponse} in frontend/services/api/authApi.ts. */
public class AuthResponse {
    public String token;
    public String email;
    public String userName;
    public String role;
    public String userId;
    public Boolean emailVerified;
    public String message;

    public AuthResponse() {}
}
