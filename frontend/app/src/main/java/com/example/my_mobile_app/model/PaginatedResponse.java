package com.example.my_mobile_app.model;

import java.util.List;

/** Spring Boot Pageable JSON shape (also matches PaginatedResponse in productApi.ts). */
public class PaginatedResponse<T> {
    public List<T> content;
    public int totalPages;
    public int totalElements;
    public int number;
    public int size;

    public PaginatedResponse() {}
}
