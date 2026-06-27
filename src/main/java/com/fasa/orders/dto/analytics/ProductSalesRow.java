package com.fasa.orders.dto.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProductSalesRow {

    private Long productId;
    private String name;
    private long quantitySold;
    private BigDecimal revenue = BigDecimal.ZERO;
    private double sharePercent;

    public ProductSalesRow() {
    }

    public ProductSalesRow(Long productId, String name, long quantitySold, BigDecimal revenue) {
        this.productId = productId;
        this.name = name;
        this.quantitySold = quantitySold;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
    }

    public void applySharePercent(BigDecimal totalProductRevenue) {
        if (totalProductRevenue == null || totalProductRevenue.compareTo(BigDecimal.ZERO) <= 0) {
            sharePercent = 0;
            return;
        }
        sharePercent = revenue
                .multiply(BigDecimal.valueOf(100))
                .divide(totalProductRevenue, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(long quantitySold) {
        this.quantitySold = quantitySold;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
    }

    public double getSharePercent() {
        return sharePercent;
    }
}
