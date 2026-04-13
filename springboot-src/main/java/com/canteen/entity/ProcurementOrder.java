package com.canteen.entity;

import com.canteen.entity.enums.ProcurementStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_procurement_order")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class ProcurementOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "proc_no", unique = true, length = 32)
    private String procNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(length = 16)
    private String unit;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_cost", precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private ProcurementStatus status = ProcurementStatus.PENDING;

    @Column(name = "expected_date")
    private LocalDate expectedDate;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "approver_id")
    private Long approverId;

    @Column(length = 512)
    private String remark;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
