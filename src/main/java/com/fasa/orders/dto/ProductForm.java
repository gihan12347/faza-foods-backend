package com.fasa.orders.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Form model for admin add/edit (list fields as newline-separated text).
 */
public class ProductForm {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private boolean bestSeller;
    private String weight;
    private String image;
    private String howToUse;
    private String category;
    private Integer currentStock;
    private Integer minimumStock;
    private String ingredientsText;
    private String useForText;
    private String imagesText;

    public static ProductForm fromDto(ProductDto dto) {
        ProductForm form = new ProductForm();
        form.setId(dto.getId());
        form.setName(dto.getName());
        form.setDescription(dto.getDescription());
        form.setPrice(dto.getPrice());
        form.setOriginalPrice(dto.getOriginalPrice());
        form.setBestSeller(dto.isBestSeller());
        form.setWeight(dto.getWeight());
        form.setImage(dto.getImage());
        form.setHowToUse(dto.getHowToUse());
        form.setCategory(dto.getCategory());
        form.setCurrentStock(dto.getCurrentStock());
        form.setMinimumStock(dto.getMinimumStock());
        form.setIngredientsText(joinLines(dto.getIngredients()));
        form.setUseForText(joinLines(dto.getUseFor()));
        form.setImagesText(joinLines(dto.getImages()));
        return form;
    }

    public static ProductForm empty(Long suggestedId) {
        ProductForm form = new ProductForm();
        form.setId(suggestedId);
        form.setCurrentStock(0);
        form.setMinimumStock(0);
        form.setOriginalPrice(BigDecimal.valueOf(0));
        form.setPrice(BigDecimal.valueOf(0));
        return form;
    }

    public ProductDto toDto() {
        ProductDto dto = new ProductDto();
        dto.setId(id);
        dto.setName(trim(name));
        dto.setDescription(description != null ? description : "");
        dto.setPrice(price);
        dto.setOriginalPrice(originalPrice);
        dto.setBestSeller(bestSeller);
        dto.setWeight(trim(weight));
        dto.setImage(trim(image));
        dto.setHowToUse(howToUse != null ? howToUse : "");
        dto.setCategory(trim(category));
        dto.setCurrentStock(currentStock != null ? currentStock : 0);
        dto.setMinimumStock(minimumStock != null ? minimumStock : 0);
        dto.setIngredients(parseLines(ingredientsText));
        dto.setUseFor(parseLines(useForText));
        dto.setImages(parseLines(imagesText));
        if (dto.getImages().isEmpty() && dto.getImage() != null && !dto.getImage().isEmpty()) {
            dto.getImages().add(dto.getImage());
        }
        if ((dto.getImage() == null || dto.getImage().isEmpty()) && !dto.getImages().isEmpty()) {
            dto.setImage(dto.getImages().get(0));
        }
        return dto;
    }

    private static String joinLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(lines.get(i));
        }
        return sb.toString();
    }

    public static List<String> parseLines(String text) {
        List<String> out = new ArrayList<String>();
        if (text == null || text.trim().isEmpty()) {
            return out;
        }
        String[] parts = text.split("\\r?\\n");
        for (String part : parts) {
            String t = part.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

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

    public String getIngredientsText() {
        return ingredientsText;
    }

    public void setIngredientsText(String ingredientsText) {
        this.ingredientsText = ingredientsText;
    }

    public String getUseForText() {
        return useForText;
    }

    public void setUseForText(String useForText) {
        this.useForText = useForText;
    }

    public String getImagesText() {
        return imagesText;
    }

    public void setImagesText(String imagesText) {
        this.imagesText = imagesText;
    }
}
