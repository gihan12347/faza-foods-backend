package com.fasa.orders.repository;

import com.fasa.orders.entity.ShippingRateTiers;
import com.fasa.orders.enums.DeliveryTypes;
import com.fasa.orders.enums.RateTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingRateTiersRepository extends JpaRepository<ShippingRateTiers, Long> {

    List<ShippingRateTiers> findAllByOrderByIdAsc();

    @Query("SELECT s.price " +
            "FROM ShippingRateTiers s " +
            "WHERE s.rateType = :rateType " +
            "AND s.deliveryTypes = :deliveryType " +
            "AND :weight > s.minWeight " +
            "AND :weight <= s.maxWeight")
    Optional<BigDecimal> findPrice(
            @Param("rateType") RateTypes rateType,
            @Param("deliveryType") DeliveryTypes deliveryType,
            @Param("weight") Double weight);
}
