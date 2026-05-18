package com.example.my_mobile_app.model;

/** Mirrors {@code User} interface in frontend/services/api/authApi.ts. */
public class User {
    public String userId;
    public String userName;
    public String email;
    public String avatarUrl;
    public String role;
    public double trustScore;
    public Boolean emailVerified;

    public User() {}
}
