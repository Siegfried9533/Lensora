package com.camerashop.service;

import com.camerashop.dto.*;
import com.camerashop.entity.EmailVerificationToken;
import com.camerashop.entity.User;
import com.camerashop.entity.User.Role;
import com.camerashop.repository.UserRepository;
import com.camerashop.util.JwtUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailVerificationTokenService tokenService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .userName(request.getUserName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .trustScore(100)
                .emailVerified(false)
                .provider("local")
                .build();

        userRepository.save(user);

        // Create verification token and send email
        EmailVerificationToken token = tokenService.createVerificationToken(user);
        try {
            emailService.sendEmailVerification(user.getEmail(), user.getUserName(), token.getToken());
        } catch (MessagingException e) {
            // Log error but don't fail registration
            System.err.println("Failed to send verification email: " + e.getMessage());
        }

        // Generate JWT token (but user should verify email before full access)
        String jwtToken = jwtUtil.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build()
        );

        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .userName(user.getUserName())
                .role(user.getRole().name())
                .userId(user.getUserId())
                .emailVerified(false)
                .message("Registration successful. Please check your email to verify your account.")
                .build();
    }

    public AuthResponse registerOAuthUser(String email, String name, String provider, String providerId) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // Create new user
            user = User.builder()
                    .userName(name)
                    .email(email)
                    .password(null) // No password for OAuth users
                    .role(Role.USER)
                    .trustScore(100)
                    .emailVerified(true) // OAuth emails are pre-verified
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(user);
        } else {
            // Update existing user with OAuth info if needed
            if (user.getProvider() == null) {
                user.setProvider(provider);
                user.setProviderId(providerId);
                user.setEmailVerified(true);
                userRepository.save(user);
            }
        }

        String token = jwtUtil.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password("")
                        .roles(user.getRole().name())
                        .build()
        );

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .userName(user.getUserName())
                .role(user.getRole().name())
                .userId(user.getUserId())
                .emailVerified(true)
                .build();
    }

    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenService.getVerificationToken(token);

        if (verificationToken == null) {
            throw new RuntimeException("Invalid verification token");
        }

        if (tokenService.isTokenExpired(verificationToken)) {
            throw new RuntimeException("Verification token has expired");
        }

        if (verificationToken.isUsed()) {
            throw new RuntimeException("Token has already been used");
        }

        tokenService.confirmVerification(verificationToken);
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        EmailVerificationToken token = tokenService.createVerificationToken(user);
        try {
            emailService.sendEmailVerification(user.getEmail(), user.getUserName(), token.getToken());
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        String token = jwtUtil.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build()
        );

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .userName(user.getUserName())
                .role(user.getRole().name())
                .userId(user.getUserId())
                .build();
    }

    public UserDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .trustScore(user.getTrustScore())
                .build();
    }

    public UserDTO updateAvatar(String email, String avatarUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return UserDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .trustScore(user.getTrustScore())
                .build();
    }

    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
