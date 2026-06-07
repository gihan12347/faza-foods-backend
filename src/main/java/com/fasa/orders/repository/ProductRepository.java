package com.fasa.orders.repository;

import com.fasa.orders.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findAllByOrderByIdAsc();

    @Query("SELECT p FROM ProductEntity p WHERE p.currentStock < p.minimumStock ORDER BY p.currentStock ASC, p.id ASC")
    List<ProductEntity> findLowStockProducts();

    Optional<ProductEntity> findByNameAndId(String name, Long id);
}
