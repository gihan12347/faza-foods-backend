package com.fasa.orders.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Product payload matching the storefront JSON shape ({@code products[]} items).
 */
public class ProductDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;

    @JsonProperty("isBestSeller")
    private boolean bestSeller;

    @JsonProperty("isDeliveryFree")
    private boolean deliveryFree;

    private String weight;
    private String image;
    private String howToUse;
    private String category;
    private Integer currentStock;
    private Integer minimumStock;
    private List<String> ingredients = new ArrayList<String>();

    @JsonProperty("useFor")
    private List<String> useFor = new ArrayList<String>();

    private List<String> images = new ArrayList<String>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    @JsonProperty("isBestSeller")
    public boolean isBestSeller() {
        return bestSeller;
    }

    @JsonProperty("isBestSeller")
    public void setBestSeller(boolean bestSeller) {
        this.bestSeller = bestSeller;
    }

    @JsonProperty("isDeliveryFree")
    public boolean isDeliveryFree() {
        return deliveryFree;
    }

    @JsonProperty("isDeliveryFree")
    public void setDeliveryFree(boolean deliveryFree) {
        this.deliveryFree = deliveryFree;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getHowToUse() {
        return howToUse;
    }

    public void setHowToUse(String howToUse) {
        this.howToUse = howToUse;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients != null ? ingredients : new ArrayList<String>();
    }

    public List<String> getUseFor() {
        return useFor;
    }

    public void setUseFor(List<String> useFor) {
        this.useFor = useFor != null ? useFor : new ArrayList<String>();
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images != null ? images : new ArrayList<String>();
    }
}
