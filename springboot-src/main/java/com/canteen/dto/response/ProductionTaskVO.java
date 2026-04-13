package com.canteen.dto.response;

import lombok.Data;

@Data
public class ProductionTaskVO {
    private Long id;
    private String name;
    private String startTime;
    private String endTime;
    private String status;
    private Integer progress;
    private String chef;
    private Long orderId;
    private String orderNo;
}
