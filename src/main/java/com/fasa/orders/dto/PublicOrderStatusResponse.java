package com.fasa.orders.dto;

import com.fasa.orders.entity.OrderItemEntity;

import java.util.List;

/**
 * Minimal, non-sensitive payload for public order status lookup by token (order id).
 */
public class PublicOrderStatusResponse {

    private Long orderId;
    /** Human-readable status, e.g. "Pending", "Processing". */
    private String orderStatus;
    /** Short customer-facing explanation. */
    private String message;
    private List<OrderItemEntityDTO> orderItemEntities;

    public PublicOrderStatusResponse(Long orderId, String orderStatus, String message, List<OrderItemEntityDTO> orderItemEntities) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.message = message;
        this.orderItemEntities = orderItemEntities;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public List<OrderItemEntityDTO> getOrderItemEntities() {
        return orderItemEntities;
    }

    public void setOrderItemEntities(List<OrderItemEntityDTO> orderItemEntities) {
        this.orderItemEntities = orderItemEntities;
    }
}
