package com.fasa.orders.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailySalesRow {

    private LocalDate date;
    private long orderCount;
    private long itemsSold;
    private BigDecimal revenue = BigDecimal.ZERO;

    public DailySalesRow() {
    }

    public DailySalesRow(LocalDate date, long orderCount, long itemsSold, BigDecimal revenue) {
        this.date = date;
        this.orderCount = orderCount;
        this.itemsSold = itemsSold;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

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

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
    }
}
