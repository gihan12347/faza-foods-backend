package com.fasa.orders.dto.analytics;

import java.math.BigDecimal;

public class DeliveryTypeSalesRow {

    private String deliveryType;
    private long orderCount;
    private BigDecimal revenue = BigDecimal.ZERO;

    public DeliveryTypeSalesRow() {
    }

    public DeliveryTypeSalesRow(String deliveryType, long orderCount, BigDecimal revenue) {
        this.deliveryType = deliveryType;
        this.orderCount = orderCount;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
    }
}
