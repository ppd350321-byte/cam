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
@Table(name = "t_dish")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private MenuCategory category;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(precision = 8, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "vip_price", precision = 8, scale = 2)
    private BigDecimal vipPrice;

    @Column
    private Integer stock = 0;

    @Column
    private Boolean available = true;

    @Column(name = "is_set_meal")
    private Boolean isSetMeal = false;

    @Column(length = 255)
    private String tags;

    @Column(name = "original_price", precision = 8, scale = 2)
    private BigDecimal originalPrice;

    @Column
    private Integer sales = 0;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "use_custom_vip_price")
    private Boolean useCustomVipPrice = false;

    @Column(name = "cost_price", precision = 8, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
