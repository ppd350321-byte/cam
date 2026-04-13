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
@Table(name = "t_vip_level")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class VipLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @Column(nullable = false)
    private Integer level;

    @Column(name = "min_spend", precision = 10, scale = 2, nullable = false)
    private BigDecimal minSpend;

    @Column(precision = 5, scale = 2)
    private BigDecimal discount;

    @Column(name = "daily_points")
    private Integer dailyPoints;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
