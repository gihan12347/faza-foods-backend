package com.fasa.orders.repository;

import com.fasa.orders.entity.ProductEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Cacheable(value = "allProducts")
    List<ProductEntity> findAllByOrderByIdAsc();

    @Cacheable(value = "lowStockProducts")
    @Query("SELECT p FROM ProductEntity p WHERE p.currentStock < p.minimumStock ORDER BY p.currentStock ASC, p.id ASC")
    List<ProductEntity> findLowStockProducts();

    @Cacheable(
            value = "productByNameAndId",
            key = "#name + ':' + #id"
    )
    ProductEntity findByNameAndId(String name, Long id);
}
