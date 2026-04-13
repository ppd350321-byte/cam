package com.canteen.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserVO {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String department;
    private String title;
    private Boolean isVip;
    private String userTypeLabel;
    private BigDecimal balance;
    private Integer points;
    private String lastVisitAt;
    private String status;
}
