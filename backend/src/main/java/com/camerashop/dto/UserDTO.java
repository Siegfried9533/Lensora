package com.camerashop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String userId;
    private String userName;
    private String email;
    private String avatarUrl;
    private String role;
    private Integer trustScore;
    private boolean emailVerified;
    private String provider;
}
