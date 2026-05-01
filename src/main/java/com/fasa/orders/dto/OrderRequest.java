package com.fasa.orders.dto;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class OrderRequest {

    private String orderSource;
    private String placedAt;

    /** Subtotal of line items (before delivery). */
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal orderPrice;

    /** Shipping / delivery fee. */
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal deliveryPrice;

    @Valid
    @NotEmpty
    private List<OrderItemRequest> items;

    @Valid
    private DeliveryDetailsRequest deliveryDetails;

    public String getOrderSource() {
        return orderSource;
    }

    public void setOrderSource(String orderSource) {
        this.orderSource = orderSource;
    }

    public String getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(String placedAt) {
        this.placedAt = placedAt;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }

    public BigDecimal getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(BigDecimal deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public DeliveryDetailsRequest getDeliveryDetails() {
        return deliveryDetails;
    }

    public void setDeliveryDetails(DeliveryDetailsRequest deliveryDetails) {
        this.deliveryDetails = deliveryDetails;
    }
}
