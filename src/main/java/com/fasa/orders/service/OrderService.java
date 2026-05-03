package com.fasa.orders.service;

import com.fasa.orders.dto.DeliveryDetailsRequest;
import com.fasa.orders.dto.OrderItemRequest;
import com.fasa.orders.dto.OrderRequest;
import com.fasa.orders.entity.OrderEntity;
import com.fasa.orders.entity.OrderItemEntity;
import com.fasa.orders.entity.OrderStatus;
import com.fasa.orders.repository.OrderRepository;
import com.fasa.orders.repository.OrderSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {
    private static final long ORDER_ID_MIN = 100000L;
    private static final long ORDER_ID_MAX = 999999999L;
    private static final int ORDER_ID_MAX_RETRIES = 25;

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Page<OrderEntity> findOrdersPage(String search, String statusParam, int page, int size) {
        OrderStatus statusFilter = resolveStatus(statusParam);
        Specification<OrderEntity> spec = Specification
                .where(OrderSpecifications.hasStatus(statusFilter))
                .and(OrderSpecifications.matchesSearch(search));

        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(50, Math.max(1, size)),
                Sort.by(Sort.Direction.ASC, "createdAt")
        );
        return orderRepository.findAll(spec, pageable);
    }

    private static OrderStatus resolveStatus(String statusParam) {
        return parseStatus(statusParam).orElse(null);
    }

    @Transactional
    public OrderEntity saveOrder(OrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setId(generateRandomUniqueOrderId());
        order.setOrderSource(request.getOrderSource());
        order.setPlacedAt(request.getPlacedAt());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal orderPrice = request.getOrderPrice() != null
                ? request.getOrderPrice().setScale(2, RoundingMode.HALF_UP)
                : sumItemsSubtotal(request);
        BigDecimal deliveryPrice = request.getDeliveryPrice() != null
                ? request.getDeliveryPrice().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        order.setOrderPrice(orderPrice);
        order.setDeliveryPrice(deliveryPrice);

        DeliveryDetailsRequest details = request.getDeliveryDetails();
        if (details != null) {
            order.setDeliveryType(details.getDeliveryType());
            order.setAddressLine1(details.getAddressLine1());
            order.setAddressLine2(details.getAddressLine2());
            order.setDistrict(details.getDistrict());
            order.setWhatsappNumber(details.getWhatsappNumber());
            order.setOtherPhoneNumber(details.getOtherPhoneNumber());
        }

        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItemEntity item = new OrderItemEntity();
            item.setProductId(itemRequest.getId());
            item.setName(itemRequest.getName());
            item.setPrice(itemRequest.getPrice());
            item.setQuantity(itemRequest.getQuantity());
            item.setWeight(itemRequest.getWeight());
            order.addItem(item);
        }

        return orderRepository.saveAndFlush(order);
    }

    private long generateRandomUniqueOrderId() {
        for (int attempt = 0; attempt < ORDER_ID_MAX_RETRIES; attempt++) {
            long candidate = ThreadLocalRandom.current().nextLong(ORDER_ID_MIN, ORDER_ID_MAX + 1);
            if (!orderRepository.existsById(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to allocate a unique order ID. Please retry.");
    }

    private static BigDecimal sumItemsSubtotal(OrderRequest request) {
        BigDecimal sum = BigDecimal.ZERO;
        if (request.getItems() == null) {
            return sum.setScale(2, RoundingMode.HALF_UP);
        }
        for (OrderItemRequest line : request.getItems()) {
            if (line.getPrice() == null) {
                continue;
            }
            int qty = line.getQuantity() == null ? 1 : Math.max(1, line.getQuantity());
            BigDecimal lineTotal = line.getPrice().multiply(BigDecimal.valueOf(qty));
            sum = sum.add(lineTotal);
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    public static Optional<OrderStatus> parseStatus(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Optional.empty();
        }
        try {
            return Optional.of(OrderStatus.valueOf(raw.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
