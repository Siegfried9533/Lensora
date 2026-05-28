package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.CategoryDTO;
import com.example.backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/by-type/{type}")
    public ResponseEntity<ApiResponse> getCategoriesByType(@PathVariable String type) {
        List<CategoryDTO> categories = categoryService.getCategoriesByType(type);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
