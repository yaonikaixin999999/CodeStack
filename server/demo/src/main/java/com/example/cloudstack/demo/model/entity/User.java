package com.example.cloudstack.demo.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 500)
    private String avatar;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 100)
    private String profession;

    @Column(length = 100)
    private String location;

    @Column(length = 255)
    private String website;

    @Column(columnDefinition = "TINYINT UNSIGNED DEFAULT 1")
    @Builder.Default
    private Integer level = 1;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    @Builder.Default
    private Integer status = 1;

    @Column(length = 20)
    @Builder.Default
    private String role = "user";

    @Column(name = "is_admin", columnDefinition = "TINYINT(1) DEFAULT 0")
    @Builder.Default
    private Boolean isAdmin = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
