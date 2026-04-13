package com.camerashop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String email;
    private String userName;
    private String role;
    private String userId;
    private boolean emailVerified;
    private String message;
}
