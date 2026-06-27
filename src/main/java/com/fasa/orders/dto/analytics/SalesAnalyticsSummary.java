package com.fasa.orders.dto.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SalesAnalyticsSummary {

    private long orderCount;
    private long itemsSold;
    private long uniqueProductsSold;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal productRevenue = BigDecimal.ZERO;
    private BigDecimal deliveryRevenue = BigDecimal.ZERO;

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }

    public long getItemsSold() {
        return itemsSold;
    }

    public void setItemsSold(long itemsSold) {
        this.itemsSold = itemsSold;
    }

    public long getUniqueProductsSold() {
        return uniqueProductsSold;
    }

    public void setUniqueProductsSold(long uniqueProductsSold) {
        this.uniqueProductsSold = uniqueProductsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public BigDecimal getProductRevenue() {
        return productRevenue;
    }

    public void setProductRevenue(BigDecimal productRevenue) {
        this.productRevenue = productRevenue != null ? productRevenue : BigDecimal.ZERO;
    }

    public BigDecimal getDeliveryRevenue() {
        return deliveryRevenue;
    }

    public void setDeliveryRevenue(BigDecimal deliveryRevenue) {
        this.deliveryRevenue = deliveryRevenue != null ? deliveryRevenue : BigDecimal.ZERO;
    }

    public BigDecimal getAverageOrderValue() {
        if (orderCount <= 0) {
            return BigDecimal.ZERO;
        }
        return totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);
    }
}
