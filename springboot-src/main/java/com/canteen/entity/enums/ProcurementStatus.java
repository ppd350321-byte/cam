package com.canteen.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProcurementStatus {
    PENDING("pending"),
    APPROVED("approved"),
    IN_TRANSIT("in_transit"),
    RECEIVED("received"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    ProcurementStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
