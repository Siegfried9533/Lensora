package com.example.my_mobile_app.api.dto;

public class RegisterRequest {
    public String userName;
    public String email;
    public String password;

    public RegisterRequest() {}

    public RegisterRequest(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }
}
