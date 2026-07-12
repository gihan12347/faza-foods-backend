package com.fasa.orders.entity;

import com.fasa.orders.enums.DeliveryTypes;
import com.fasa.orders.enums.RateTypes;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shipping_rate_tiers")
public class ShippingRateTiers {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", nullable = false)
    private RateTypes rateType;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private DeliveryTypes deliveryTypes;

    @Column(name = "min_weight", nullable = false)
    private Double minWeight;

    @Column(name = "max_weight", nullable = false)
    private Double maxWeight;

    @Column(name = "price", nullable =false)
    private BigDecimal price;

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