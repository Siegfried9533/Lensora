package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Users") // Trong JPA dùng @Table để đặt tên bảng
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String userName;
    private String password;
    private String email;
    private String role; // user | admin
    private int trustScore = 100; // Mặc định 100 điểm uy tín

    // Constructor
    public User(String userName, String password, String email, String role, int trustScore) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.role = role; // user | admin
        this.trustScore = trustScore;
    }
}