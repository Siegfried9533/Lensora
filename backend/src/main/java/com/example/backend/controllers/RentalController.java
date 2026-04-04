package com.example.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.backend.dto.RentalRequest;
import com.example.backend.entity.Rental;
import com.example.backend.services.RentalService;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/rentals")
@CrossOrigin(origins = "*")
public class RentalController {

    @Autowired
    private RentalService rentalService;

    // APT gate Tạo đơn hàng thuê máy ảnh
    @PostMapping("/create")
    public ResponseEntity<?> createRental(@RequestBody RentalRequest request) {
        try {
            Rental rental = rentalService.createRental(
                    request.getCustomerId(),
                    request.getCameraId(),
                    request.getRentalDays());
            return ResponseEntity.ok(rental);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // API gate trả máy ảnh
    @PostMapping("/{id}/return")
    public ResponseEntity<?> returnCamera(@PathVariable Long id) {
        try {
            Rental rental = rentalService.returnCamera(id);
            return ResponseEntity.ok(rental);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
