package com.fasa.orders.service;

import com.fasa.orders.dto.ShippingRateForm;
import com.fasa.orders.entity.ShippingRateTiers;
import com.fasa.orders.repository.ShippingRateTiersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ShippingRateService {

    private final ShippingRateTiersRepository shippingRateTiersRepository;

    public ShippingRateService(ShippingRateTiersRepository shippingRateTiersRepository) {
        this.shippingRateTiersRepository = shippingRateTiersRepository;
    }

    @Transactional(readOnly = true)
    public List<ShippingRateTiers> findAll() {
        return shippingRateTiersRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Optional<ShippingRateForm> findFormById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return shippingRateTiersRepository.findById(id).map(ShippingRateForm::fromEntity);
    }

    @Transactional(readOnly = true)
    public long suggestNextId() {
        return shippingRateTiersRepository.findAllByOrderByIdAsc().stream()
                .mapToLong(ShippingRateTiers::getId)
                .max()
                .orElse(0L) + 1L;
    }

    @Transactional
    public ShippingRateTiers create(ShippingRateForm form) {
        validate(form);
        if (form.getId() == null) {
            form.setId(suggestNextId());
        }
        if (shippingRateTiersRepository.existsById(form.getId())) {
            throw new IllegalArgumentException("Shipping rate ID already exists: " + form.getId());
        }
        return shippingRateTiersRepository.save(form.toEntity());
    }

    @Transactional
    public ShippingRateTiers update(Long id, ShippingRateForm form) {
        if (id == null) {
            throw new IllegalArgumentException("Shipping rate ID is required.");
        }
        form.setId(id);
        validate(form);
        ShippingRateTiers existing = shippingRateTiersRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shipping rate not found: " + id));
        form.applyTo(existing);
        return shippingRateTiersRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (id == null || !shippingRateTiersRepository.existsById(id)) {
            throw new IllegalArgumentException("Shipping rate not found.");
        }
        shippingRateTiersRepository.deleteById(id);
    }

    private static void validate(ShippingRateForm form) {
        if (form == null) {
            throw new IllegalArgumentException("Shipping rate data is required.");
        }
        if (form.getRateType() == null) {
            throw new IllegalArgumentException("Rate type is required.");
        }
        if (form.getDeliveryTypes() == null) {
            throw new IllegalArgumentException("Delivery method is required.");
        }
        if (form.getMinWeight() == null || form.getMinWeight() < 0) {
            throw new IllegalArgumentException("Min weight must be zero or greater.");
        }
        if (form.getMaxWeight() == null || form.getMaxWeight() < 0) {
            throw new IllegalArgumentException("Max weight must be zero or greater.");
        }
        if (form.getMaxWeight() <= form.getMinWeight()) {
            throw new IllegalArgumentException("Max weight must be greater than min weight.");
        }
        if (form.getPrice() == null || form.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be zero or greater.");
        }
    }
}
