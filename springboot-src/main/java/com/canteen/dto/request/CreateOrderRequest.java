package com.canteen.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemRequest> items;

    private String paymentMethod;
    private Long couponId;
    private String remark;

    @Data
    public static class OrderItemRequest {
        private Long dishId;
        private Integer quantity;
    }
}
