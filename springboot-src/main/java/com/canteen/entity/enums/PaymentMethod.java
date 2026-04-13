package com.canteen.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    BALANCE("balance"),
    WECHAT("wechat"),
    ALIPAY("alipay"),
    UNPAID("unpaid");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static PaymentMethod fromValue(String value) {
        for (PaymentMethod m : values()) {
            if (m.value.equals(value)) return m;
        }
        throw new IllegalArgumentException("Unknown payment method: " + value);
    }
}
