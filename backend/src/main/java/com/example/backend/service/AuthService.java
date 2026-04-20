package com.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.SignUpRequest;
import com.example.backend.dto.LoginRequest;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // AuthResponse
    // SignUpRequest
    public AuthResponse signup(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists!");
        }

        User user = new User();
        user.setUserName(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        if (request.role() == null || request.role().isEmpty()) {
            user.setRole("user"); // default role
        } else {
            user.setRole(request.role());
        }
        user.setTrustScore(100); // default trust score

        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getUserId().toString(), user.getUserName(), user.getEmail(),
                user.getRole());
    }

    // login request
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.email()));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getUserId().toString(), user.getUserName(), user.getEmail(),
                user.getRole());
    }

    // get current user
    public AuthResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return new AuthResponse(null, user.getUserId().toString(), user.getUserName(), user.getEmail(),
                user.getRole());
    }

}
