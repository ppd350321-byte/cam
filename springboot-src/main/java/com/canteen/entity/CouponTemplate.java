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
@Table(name = "t_coupon_template")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class CouponTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128, nullable = false)
    private String title;

    @Column(precision = 8, scale = 2)
    private BigDecimal amount;

    @Column(name = "min_amount", precision = 8, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "total_qty")
    private Integer totalQty;

    @Column(name = "remaining_qty")
    private Integer remainingQty;

    @Column(name = "points_cost")
    private Integer pointsCost;

    @Column(name = "valid_days")
    private Integer validDays;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
