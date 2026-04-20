package com.example.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.RentalRequest;
import com.example.backend.dto.RentalResponse;
import com.example.backend.service.RentalService;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;

    // Tạo đơn thuê mới
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RentalResponse> createRental(@RequestBody RentalRequest request,
            Authentication authentication) {
        String currentEmail = authentication.getName();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rentalService.createRental(request, currentEmail));
    }

    // Xem lịch sử thuê của cá nhân (người đang đăng nhập)
    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RentalResponse>> getMyHistory(Authentication authentication) {
        return ResponseEntity.ok(rentalService.getUserRentalHistory(authentication.getName()));
    }

    // Trả tài sản
    @PostMapping("/{rentalId}/return")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RentalResponse> returnAsset(@PathVariable Long rentalId, Authentication authentication) {
        String currentEmail = authentication.getName();
        return ResponseEntity.ok(rentalService.returnAsset(rentalId, currentEmail));
    }
}
