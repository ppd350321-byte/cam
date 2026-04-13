package com.canteen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateProcurementRequest {
    @NotNull(message = "物料ID不能为空")
    private Long materialId;

    @NotNull(message = "供货商ID不能为空")
    private Long supplierId;

    @NotNull(message = "采购数量不能为空")
    private BigDecimal quantity;

    private String unit;

    private BigDecimal unitPrice;

    private LocalDate expectedDate;

    private String remark;
}
