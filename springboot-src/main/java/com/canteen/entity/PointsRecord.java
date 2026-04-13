package com.canteen.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_points_record")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class PointsRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 32, nullable = false)
    private String type;

    @Column(nullable = false)
    private Integer points;

    @Column(name = "points_after")
    private Integer pointsAfter;

    @Column(length = 255)
    private String remark;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
