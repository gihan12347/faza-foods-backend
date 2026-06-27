package com.fasa.orders.dto.analytics;

import java.math.BigDecimal;

public class DistrictSalesRow {

    private String district;
    private long orderCount;
    private BigDecimal revenue = BigDecimal.ZERO;

    public DistrictSalesRow() {
    }

    public DistrictSalesRow(String district, long orderCount, BigDecimal revenue) {
        this.district = district;
        this.orderCount = orderCount;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
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
