package com.canteen.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {
    private Long id;
    private Long dishId;
    private String name;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal price;
    private String image;
}
