package com.fasa.orders.entity;

public enum OrderStatus {
    PENDING,
    PROCESSING,
    DELIVERED,
    DONE,
    REJECT;

    public String getLabel() {
        switch (this) {
            case PROCESSING:
                return "Processing";
            case DELIVERED:
                return "Delivered";
            case DONE:
                return "Done";
            case REJECT:
                return "Reject";
            default:
                return "Pending";
        }
    }
}
