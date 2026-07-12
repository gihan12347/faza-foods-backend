package com.fasa.orders.entity;

public enum OrderStatus {
    NEW,
    PENDING,
    PROCESSING,
    DELIVERED,
    DONE,
    REJECT;

    public String getLabel() {
        switch (this) {
            case NEW:
                return "new order";
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
