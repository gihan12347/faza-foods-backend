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
