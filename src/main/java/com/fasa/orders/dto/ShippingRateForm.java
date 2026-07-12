package com.fasa.orders.dto;

import com.fasa.orders.entity.ShippingRateTiers;
import com.fasa.orders.enums.DeliveryTypes;
import com.fasa.orders.enums.RateTypes;

import java.math.BigDecimal;

/**
 * Form model for shipping rate tier admin CRUD.
 */
public class ShippingRateForm {

    private Long id;
    private RateTypes rateType;
    private DeliveryTypes deliveryTypes;
    private Double minWeight;
    private Double maxWeight;
    private BigDecimal price;

    public static ShippingRateForm fromEntity(ShippingRateTiers entity) {
        ShippingRateForm form = new ShippingRateForm();
        form.setId(entity.getId());
        form.setRateType(entity.getRateType());
        form.setDeliveryTypes(entity.getDeliveryTypes());
        form.setMinWeight(entity.getMinWeight());
        form.setMaxWeight(entity.getMaxWeight());
        form.setPrice(entity.getPrice());
        return form;
    }

    public static ShippingRateForm empty(Long suggestedId) {
        ShippingRateForm form = new ShippingRateForm();
        form.setId(suggestedId);
        form.setRateType(RateTypes.normal);
        form.setDeliveryTypes(DeliveryTypes.courier);
        form.setMinWeight(0.0);
        form.setMaxWeight(0.0);
        form.setPrice(BigDecimal.ZERO);
        return form;
    }

    public ShippingRateTiers toEntity() {
        ShippingRateTiers entity = new ShippingRateTiers();
        entity.setId(id);
        entity.setRateType(rateType);
        entity.setDeliveryTypes(deliveryTypes);
        entity.setMinWeight(minWeight);
        entity.setMaxWeight(maxWeight);
        entity.setPrice(price);
        return entity;
    }

    public void applyTo(ShippingRateTiers entity) {
        entity.setRateType(rateType);
        entity.setDeliveryTypes(deliveryTypes);
        entity.setMinWeight(minWeight);
        entity.setMaxWeight(maxWeight);
        entity.setPrice(price);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RateTypes getRateType() {
        return rateType;
    }

    public void setRateType(RateTypes rateType) {
        this.rateType = rateType;
    }

    public DeliveryTypes getDeliveryTypes() {
        return deliveryTypes;
    }

    public void setDeliveryTypes(DeliveryTypes deliveryTypes) {
        this.deliveryTypes = deliveryTypes;
    }

    public Double getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(Double minWeight) {
        this.minWeight = minWeight;
    }

    public Double getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(Double maxWeight) {
        this.maxWeight = maxWeight;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
