package com.canteen.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_admin")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT UNSIGNED")
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

    @Column(length = 64)
    private String title;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "t_admin_role",
            joinColumns = @JoinColumn(name = "admin_id", columnDefinition = "BIGINT UNSIGNED"),
            inverseJoinColumns = @JoinColumn(name = "role_id", columnDefinition = "BIGINT UNSIGNED"))
    private Set<Role> roles = new HashSet<>();
}
