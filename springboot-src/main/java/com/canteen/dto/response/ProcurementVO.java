package com.canteen.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProcurementVO {
    private Long id;
    private String procNo;
    private String materialName;
    private String item;
    private String supplierName;
    private String supplier;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal totalCost;
    private String cost;
    private String status;
    private String statusLabel;
    private String expectedDate;
    private String createdAt;
}
