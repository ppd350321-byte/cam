package com.canteen.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SupplierVO {
    private Long id;
    private String name;
    private String category;
    private String contact;
    private String phone;
    private String address;
    private BigDecimal rating;
    private String status;
}
