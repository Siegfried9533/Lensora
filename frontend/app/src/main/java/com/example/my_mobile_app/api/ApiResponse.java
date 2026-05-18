package com.example.my_mobile_app.api;

/**
 * Generic envelope returned by the Spring Boot backend
 * (see backend com.camerashop.dto.ApiResponse).
 */
public class ApiResponse<T> {
    public boolean success;
    public String message;
    public T data;

    public ApiResponse() {}
}
