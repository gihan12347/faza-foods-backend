package com.fasa.orders.dto;

/**
 * Admin notification when {@code currentStock < minimumStock}.
 */
public class LowStockNotificationDto {

    private Long productId;
    private String productName;
    private Integer currentStock;
    private Integer minimumStock;

    public LowStockNotificationDto() {
    }

    public LowStockNotificationDto(Long productId, String productName, Integer currentStock, Integer minimumStock) {
        this.productId = productId;
        this.productName = productName;
        this.currentStock = currentStock;
        this.minimumStock = minimumStock;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }
}
