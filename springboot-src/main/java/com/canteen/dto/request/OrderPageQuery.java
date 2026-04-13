package com.canteen.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderPageQuery extends PageQuery {
    private String orderStatus;
    private String paymentMethod;
}
