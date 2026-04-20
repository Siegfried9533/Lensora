package com.example.backend.service;

import com.example.backend.entity.EmailVerificationToken;
import com.example.backend.entity.User;
import com.example.backend.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailVerificationTokenService {

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    private static final int EXPIRATION_HOURS = 24;

    public EmailVerificationToken createVerificationToken(User user) {
        // Delete any existing token for this user
        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(EXPIRATION_HOURS))
                .build();

        return tokenRepository.save(verificationToken);
    }

    public EmailVerificationToken getVerificationToken(String token) {
        return tokenRepository.findByToken(token).orElse(null);
    }

    @Transactional
    public void confirmVerification(EmailVerificationToken verificationToken) {
        verificationToken.setUsed(true);
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        tokenRepository.save(verificationToken);
    }

    public boolean isTokenExpired(EmailVerificationToken token) {
        return token == null || token.isExpired();
    }
}
