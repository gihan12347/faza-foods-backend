package com.fasa.orders.entity;

public enum OrderStatus {
    PENDING,
    PROCESSING,
    DELIVERED;

    public String getLabel() {
        switch (this) {
            case PROCESSING:
                return "Processing";
            case DELIVERED:
                return "Delivered";
            default:
                return "Pending";
        }
    }
}
