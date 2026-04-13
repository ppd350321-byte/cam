package com.canteen.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_admin_performance")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class AdminPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private ProductionTask task;

    @Column(name = "dish_name", length = 128)
    private String dishName;

    @Column(name = "dish_price", precision = 8, scale = 2)
    private BigDecimal dishPrice;

    @Column(precision = 8, scale = 2, nullable = false)
    private BigDecimal score;

    @Column(length = 255)
    private String remark;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
