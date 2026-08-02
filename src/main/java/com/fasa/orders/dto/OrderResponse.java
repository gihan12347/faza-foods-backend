package com.fasa.orders.dto;

import java.util.Map;

public class OrderResponse {

    private String status;
    private String message;
    private Long orderId;
    private String downloadUrl;
    private Map<String, String> validationErrors;

    public OrderResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getDownloadUrl() { return downloadUrl; }

    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
