package com.fasa.orders.entity;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "original_price", nullable = false)
    private BigDecimal originalPrice;

    @Column(name = "is_best_seller", nullable = false)
    private boolean bestSeller;

    @Column(name = "is_delivery_free", nullable = false)
    private boolean deliveryFree;

    @Column(length = 40)
    private String weight;

    @Column(length = 500)
    private String image;

    @Column(name = "how_to_use", columnDefinition = "TEXT")
    private String howToUse;

    @Column(nullable = false, length = 120)
    private String category;

    @Column(name = "current_stock", nullable = false)
    private Integer currentStock = 0;

    @Column(name = "minimum_stock", nullable = false)
    private Integer minimumStock = 0;

    @ElementCollection
    @CollectionTable(name = "product_ingredients", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "ingredient", length = 200)
    @OrderColumn(name = "sort_order")
    private List<String> ingredients = new ArrayList<String>();

    @ElementCollection
    @CollectionTable(name = "product_use_for", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "use_for", length = 200)
    @OrderColumn(name = "sort_order")
    private List<String> useFor = new ArrayList<String>();

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", length = 500)
    @OrderColumn(name = "sort_order")
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

    public boolean isBestSeller() {
        return bestSeller;
    }

    public void setBestSeller(boolean bestSeller) {
        this.bestSeller = bestSeller;
    }

    public boolean isDeliveryFree() {
        return deliveryFree;
    }

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
        this.currentStock = currentStock != null ? currentStock : 0;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock != null ? minimumStock : 0;
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
