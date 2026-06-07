package com.fasa.orders.service;

import com.fasa.orders.dto.LowStockNotificationDto;
import com.fasa.orders.dto.ProductDto;
import com.fasa.orders.dto.ProductListResponse;
import com.fasa.orders.entity.ProductEntity;
import com.fasa.orders.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ProductListResponse findAllForApi() {
        List<ProductDto> products = new ArrayList<ProductDto>();
        for (ProductEntity entity : productRepository.findAllByOrderByIdAsc()) {
            products.add(toDto(entity));
        }
        return new ProductListResponse(products);
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> findAllEntities() {
        return productRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Optional<ProductDto> findDtoById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return productRepository.findById(id).map(ProductService::toDto);
    }

    @Transactional(readOnly = true)
    public long suggestNextId() {
        return productRepository.findAllByOrderByIdAsc().stream()
                .mapToLong(ProductEntity::getId)
                .max()
                .orElse(0L) + 1L;
    }

    @Transactional
    public ProductDto create(ProductDto dto) {
        validate(dto);
        if (dto.getId() == null) {
            dto.setId(suggestNextId());
        }
        if (productRepository.existsById(dto.getId())) {
            throw new IllegalArgumentException("Product ID already exists: " + dto.getId());
        }
        productRepository.save(toEntity(dto));
        return dto;
    }

    @Transactional
    public ProductDto update(Long id, ProductDto dto) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID is required.");
        }
        validate(dto);
        ProductEntity existing = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        applyDto(existing, dto);
        productRepository.save(existing);
        dto.setId(id);
        return dto;
    }

    @Transactional
    public void delete(Long id) {
        if (id == null || !productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found.");
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public void saveFromDto(ProductDto dto) {
        ProductEntity entity = toEntity(dto);
        productRepository.save(entity);
    }

    private static void validate(ProductDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Product data is required.");
        }
        if (!StringUtils.hasText(dto.getName())) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (!StringUtils.hasText(dto.getCategory())) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO)  < 0) {
            throw new IllegalArgumentException("Price must be zero or greater.");
        }
        if (dto.getOriginalPrice() == null || dto.getOriginalPrice().compareTo(BigDecimal.ZERO)  < 0) {
            throw new IllegalArgumentException("Original price must be zero or greater.");
        }
    }

    private static void applyDto(ProductEntity entity, ProductDto dto) {
        entity.setName(dto.getName().trim());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setOriginalPrice(dto.getOriginalPrice());
        entity.setBestSeller(dto.isBestSeller());
        entity.setWeight(dto.getWeight());
        entity.setImage(dto.getImage());
        entity.setHowToUse(dto.getHowToUse());
        entity.setCategory(dto.getCategory().trim());
        entity.setCurrentStock(dto.getCurrentStock());
        entity.setMinimumStock(dto.getMinimumStock());
        entity.getIngredients().clear();
        entity.getIngredients().addAll(dto.getIngredients());
        entity.getUseFor().clear();
        entity.getUseFor().addAll(dto.getUseFor());
        entity.getImages().clear();
        entity.getImages().addAll(dto.getImages());
    }

    public static ProductDto toDto(ProductEntity entity) {
        ProductDto dto = new ProductDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(nullToEmpty(entity.getDescription()));
        dto.setPrice(entity.getPrice());
        dto.setOriginalPrice(entity.getOriginalPrice());
        dto.setBestSeller(entity.isBestSeller());
        dto.setWeight(entity.getWeight());
        dto.setImage(entity.getImage());
        dto.setHowToUse(nullToEmpty(entity.getHowToUse()));
        dto.setCategory(entity.getCategory());
        dto.setCurrentStock(entity.getCurrentStock());
        dto.setMinimumStock(entity.getMinimumStock());
        dto.setIngredients(new ArrayList<String>(entity.getIngredients()));
        dto.setUseFor(new ArrayList<String>(entity.getUseFor()));
        dto.setImages(new ArrayList<String>(entity.getImages()));
        return dto;
    }

    public static ProductEntity toEntity(ProductDto dto) {
        ProductEntity entity = new ProductEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setOriginalPrice(dto.getOriginalPrice());
        entity.setBestSeller(dto.isBestSeller());
        entity.setWeight(dto.getWeight());
        entity.setImage(dto.getImage());
        entity.setHowToUse(dto.getHowToUse());
        entity.setCategory(dto.getCategory());
        entity.setCurrentStock(dto.getCurrentStock());
        entity.setMinimumStock(dto.getMinimumStock());
        entity.setIngredients(new ArrayList<String>(dto.getIngredients()));
        entity.setUseFor(new ArrayList<String>(dto.getUseFor()));
        entity.setImages(new ArrayList<String>(dto.getImages()));
        return entity;
    }

    @Transactional(readOnly = true)
    public List<LowStockNotificationDto> findLowStockNotifications() {
        List<LowStockNotificationDto> notifications = new ArrayList<LowStockNotificationDto>();
        for (ProductEntity entity : productRepository.findLowStockProducts()) {
            notifications.add(new LowStockNotificationDto(
                    entity.getId(),
                    entity.getName(),
                    entity.getCurrentStock(),
                    entity.getMinimumStock()));
        }
        return notifications;
    }

    @Transactional(readOnly = true)
    public long countLowStockBelowMinimum() {
        return productRepository.findLowStockProducts().size();
    }

    public static boolean isBelowMinimumStock(Integer currentStock, Integer minimumStock) {
        if (currentStock == null || minimumStock == null) {
            return false;
        }
        return currentStock < minimumStock;
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
