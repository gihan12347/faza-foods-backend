package com.fasa.orders.entity;

public enum OrderResponseStatus {

    SUCCESS("Order submitted successfully & we will notify you soon. Your Order ID: "),
    FAIL("Validation failed");

    private final String message;

    OrderResponseStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}