package com.camerashop.controller;

import com.camerashop.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class HealthController {

    @GetMapping("/api/health")
    public ResponseEntity<ApiResponse> health() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "UP")));
    }
}
