package com.camerashop.service;

import com.camerashop.dto.*;
import com.camerashop.entity.EmailVerificationToken;
import com.camerashop.entity.PasswordResetToken;
import com.camerashop.entity.User;
import com.camerashop.entity.User.Role;
import com.camerashop.exception.ResourceNotFoundException;
import com.camerashop.repository.PasswordResetTokenRepository;
import com.camerashop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailVerificationTokenService tokenService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    public AuthResponse register(RegisterRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email đã được đăng ký");
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
            System.out.println("User saved: " + user.getEmail());

            // Gửi thông báo chào mừng
            try {
                notificationService.createWelcomeNotification(user.getUserId());
            } catch (Exception e) {
                System.err.println("Failed to send welcome notification: " + e.getMessage());
            }

            // Tạo token xác minh email (an toàn - fail-safe)
            EmailVerificationToken token = null;
            try {
                token = tokenService.createVerificationToken(user);
                System.out.println("Verification token created: " + token.getToken());
            } catch (Exception e) {
                System.err.println("Failed to create verification token: " + e.getMessage());
            }

            // Gửi email xác minh (an toàn - không làm hỏng đăng ký nếu gửi email thất bại)
            if (token != null) {
                try {
                    emailService.sendEmailVerification(user.getEmail(), user.getUserName(), token.getToken());
                    System.out.println("Verification email sent");
                } catch (Exception e) {
                    // Ghi log lỗi nhưng không làm hỏng đăng ký - dịch vụ email có thể chưa được cấu hình
                    System.err.println("Failed to send verification email (this is OK for development): " + e.getMessage());
                }
            }

            // Tạo JWT token bằng JwtService với userId làm subject
            String jwtToken = jwtService.generateToken(user);

            return AuthResponse.builder()
                    .token(jwtToken)
                    .email(user.getEmail())
                    .userName(user.getUserName())
                    .role(user.getRole().name())
                    .userId(user.getUserId())
                    .emailVerified(false)
                    .message("Đăng ký thành công. Vui lòng kiểm tra email để xác minh tài khoản của bạn.")
                    .build();
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Đăng ký thất bại: " + e.getMessage(), e);
        }
    }

    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenService.getVerificationToken(token);

        if (verificationToken == null) {
            throw new RuntimeException("Mã xác minh không hợp lệ");
        }

        if (tokenService.isTokenExpired(verificationToken)) {
            throw new RuntimeException("Mã xác minh đã hết hạn");
        }

        if (verificationToken.isUsed()) {
            throw new RuntimeException("Mã đã được sử dụng");
        }

        tokenService.confirmVerification(verificationToken);
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email đã được xác minh");
        }

        EmailVerificationToken token = tokenService.createVerificationToken(user);
        try {
            emailService.sendEmailVerification(user.getEmail(), user.getUserName(), token.getToken());
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
        }
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không hợp lệ"));

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .userName(user.getUserName())
                .role(user.getRole().name())
                .userId(user.getUserId())
                .emailVerified(user.isEmailVerified())
                .build();
    }

    public UserDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        return UserDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .trustScore(user.getTrustScore())
                .emailVerified(user.isEmailVerified())
                .build();
    }

    public UserDTO updateAvatar(String email, String avatarUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return UserDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .trustScore(user.getTrustScore())
                .emailVerified(user.isEmailVerified())
                .build();
    }

    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email này"));

        // Vô hiệu hóa các token hiện có của người dùng này
        passwordResetTokenRepository.deleteByUserId(user.getUserId());

        String tokenValue = java.util.UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(java.time.LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUserName(), tokenValue);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
        }
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Mã đặt lại mật khẩu không hợp lệ hoặc đã hết hạn"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Mã đặt lại mật khẩu đã được sử dụng");
        }
        if (resetToken.isExpired()) {
            throw new RuntimeException("Mã đặt lại mật khẩu đã hết hạn");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
