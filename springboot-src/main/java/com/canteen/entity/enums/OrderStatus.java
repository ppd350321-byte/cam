package com.canteen.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    PENDING_PAYMENT("pending_payment"),
    PENDING_ACCEPT("pending_accept"),
    PREPARING("preparing"),
    PENDING_PICKUP("pending_pickup"),
    DELIVERING("delivering"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
    PENDING_CANCEL("pending_cancel");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static OrderStatus fromValue(String value) {
        for (OrderStatus s : values()) {
            if (s.value.equals(value)) return s;
        }
        throw new IllegalArgumentException("Unknown order status: " + value);
    }
}
