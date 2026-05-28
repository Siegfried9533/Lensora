package com.camerashop.controller;

import com.camerashop.dto.*;
import com.camerashop.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email hoặc mật khẩu không hợp lệ"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Đăng nhập thất bại: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            UserDTO user = authService.getCurrentUser(userDetails.getUsername());
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/avatar")
    public ResponseEntity<ApiResponse> updateAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        try {
            UserDTO user = authService.updateAvatar(userDetails.getUsername(), body.get("avatarUrl"));
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        try {
            authService.changePassword(userDetails.getUsername(), body.get("oldPassword"), body.get("newPassword"));
            return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse> resendVerificationEmail(@RequestBody Map<String, String> body) {
        try {
            authService.resendVerificationEmail(body.get("email"));
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            authService.forgotPassword(body.get("email"));
            return ResponseEntity.ok(ApiResponse.success("Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư của bạn."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody Map<String, String> body) {
        try {
            authService.resetPassword(body.get("token"), body.get("newPassword"));
            return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập ngay bây giờ."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
