package com.canteen.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private String customerName;
    private List<OrderItemVO> items;
    private String itemsSummary;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String orderStatusLabel;
    private String paymentMethod;
    private String paymentMethodLabel;
    private String createdAt;
    private String displayTime;
    private String pickupTime;
}
