package com.canteen.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_user")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String username;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(length = 128)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "real_name", length = 64)
    private String realName;

    @Column(length = 64)
    private String department;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "is_vip")
    private Boolean isVip = false;

    @Column(name = "vip_level")
    private Integer vipLevel = 0;

    @Column(name = "vip_expires_at")
    private LocalDateTime vipExpiresAt;

    @Column
    private Integer points = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(length = 16)
    private String status = "active";

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
