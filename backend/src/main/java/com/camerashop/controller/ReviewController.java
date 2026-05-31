package com.camerashop.controller;

import com.camerashop.dto.ApiResponse;
import com.camerashop.entity.User;
import com.camerashop.exception.ResourceNotFoundException;
import com.camerashop.repository.ReviewRepository;
import com.camerashop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/me/count")
    public ResponseEntity<ApiResponse> countMyReviews(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return ResponseEntity.ok(ApiResponse.success(reviewRepository.countByUserUserId(user.getUserId())));
    }
}
