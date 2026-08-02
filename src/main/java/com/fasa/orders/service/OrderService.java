package com.fasa.orders.service;

import com.fasa.orders.dto.*;
import com.fasa.orders.entity.OrderEntity;
import com.fasa.orders.entity.OrderItemEntity;
import com.fasa.orders.entity.OrderStatus;
import com.fasa.orders.entity.ProductEntity;
import com.fasa.orders.enums.DeliveryTypes;
import com.fasa.orders.enums.RateTypes;
import com.fasa.orders.repository.OrderRepository;
import com.fasa.orders.repository.OrderSpecifications;
import com.fasa.orders.repository.ProductRepository;
import com.fasa.orders.repository.ShippingRateTiersRepository;
import com.fasa.orders.utils.Utils;
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
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.fasa.orders.constants.Constant.*;
import static com.fasa.orders.utils.Utils.deliveryTypeFromString;

@Service
public class OrderService {
    private static final long ORDER_ID_MIN = 100000L;
    private static final long ORDER_ID_MAX = 999999999L;
    private static final int ORDER_ID_MAX_RETRIES = 25;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ShippingRateTiersRepository shippingRateTiersRepository;
    private static final Set<String> SPECIAL_RATE_DISTRICTS = new HashSet<>(Arrays.asList(
            "ampara",
            "anuradhapura",
            "batticaloa",
            "trincomalee",
            "jaffna"
    ));

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, ShippingRateTiersRepository shippingRateTiersRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.shippingRateTiersRepository = shippingRateTiersRepository;
    }

    /**
     * Public lookup: {@code token} is the numeric order id issued at checkout.
     * Returns empty if the token is missing, not numeric, or no matching order.
     */
    @Transactional(readOnly = true)
    public Optional<OrderEntity> findOrderWithItems(Long orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        return orderRepository.findByIdWithItems(orderId);
    }

    @Transactional(readOnly = true)
    public Optional<PublicOrderStatusResponse> findPublicStatusByOrderToken(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            return Optional.empty();
        }
        String trimmed = rawToken.trim();
        long orderId;
        try {
            orderId = Long.parseLong(trimmed);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
        return orderRepository.findById(orderId).map(this::toPublicOrderStatus);
    }

    private PublicOrderStatusResponse toPublicOrderStatus(OrderEntity order) {
        OrderStatus st = order.getStatus();
        String label = st.getLabel();
        List<OrderItemEntityDTO> items =
                order.getItems()
                        .stream()
                        .map(i -> new OrderItemEntityDTO(
                                i.getName(),
                                i.getQuantity()
                        ))
                        .collect(Collectors.toList());
        return new PublicOrderStatusResponse(order.getId(), label, publicStatusMessage(st), items);
    }

    private static String publicStatusMessage(OrderStatus status) {
        if (status == null) {
            return "We are reviewing your order.";
        }
        switch (status) {
            case PROCESSING:
                return "Your order is being prepared.";
            case DELIVERED:
                return "Your order is out for delivery or has been delivered.";
            case DONE:
                return "Your order is complete. Thank you for shopping with us.";
            case REJECT:
                return "This order could not be fulfilled. Please contact us if you need help.";
            case PENDING:
            default:
                return "We have received your order and will update you soon.";
        }
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

        DeliveryDetailsRequest details = request.getDeliveryDetails();
        if (details != null) {
            order.setCustomerName(details.getCustomerName());
            order.setDeliveryType(details.getDeliveryType());
            order.setAddressLine1(details.getAddressLine1());
            order.setAddressLine2(details.getAddressLine2());
            order.setDistrict(details.getDistrict());
            order.setWhatsappNumber(details.getWhatsappNumber());
            order.setOtherPhoneNumber(details.getOtherPhoneNumber());
        }

        Map<String, BigDecimal> summary = getPriceSummeryAndSaveItemsInToOderIfOrderNotNull(request, order);
        order.setOrderPrice(summary.get(SUB_TOTAL));
        order.setDeliveryPrice(summary.get(SHIPPING));
        return orderRepository.saveAndFlush(order);
    }

    private ProductEntity getProductWithUpdateInventory(ProductEntity product, Integer quantity) {
        product.setCurrentStock(product.getCurrentStock() - quantity);
        productRepository.saveAndFlush(product);
        return product;
    }

    //TODO : need to implement better encrypt method
    private long generateRandomUniqueOrderId() {
        for (int attempt = 0; attempt < ORDER_ID_MAX_RETRIES; attempt++) {
            long candidate = ThreadLocalRandom.current().nextLong(ORDER_ID_MIN, ORDER_ID_MAX + 1);
            if (!orderRepository.existsById(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to allocate a unique order ID. Please retry.");
    }

    public Map<String, BigDecimal> getPriceSummeryAndSaveItemsInToOderIfOrderNotNull(OrderRequest request, OrderEntity order) {
        Map<String, BigDecimal> summary = new LinkedHashMap<>();
        BigDecimal subTotal = BigDecimal.ZERO;
        double totalWeight = 0.0;
        boolean isDeliveryFreeItemAvailable = false;

        for (OrderItemRequest item : request.getItems()) {
            Optional<ProductEntity> ProductEntity = getProductByNameAndId(item.getName(), item.getId());
            if (ProductEntity.isPresent()) {
                ProductEntity product = ProductEntity.get();
                int quantity = item.getQuantity();
                if (product.isDeliveryFree()) {
                    isDeliveryFreeItemAvailable = true;
                }
                subTotal = subTotal.add(
                        product.getPrice().multiply(BigDecimal.valueOf(quantity))
                );
                totalWeight += Utils.parseWeightToKg(product.getWeight()) * quantity;
                if (order != null) {
                    addItemsToOrder(product, item, order);
                }
            }
        }

        DeliveryDetailsRequest delivery = request.getDeliveryDetails();
        BigDecimal shipping = getShippingPriceByWeight(
                totalWeight,
                delivery.getDeliveryType(),
                delivery.getDistrict(), isDeliveryFreeItemAvailable);
        summary.put(SUB_TOTAL, subTotal.setScale(2, RoundingMode.HALF_UP));
        summary.put(SHIPPING, shipping.setScale(2, RoundingMode.HALF_UP));
        summary.put(TOTAL, subTotal.add(shipping).setScale(2, RoundingMode.HALF_UP));
        return summary;
    }

    private void addItemsToOrder(ProductEntity productEntity, OrderItemRequest itemRequest, OrderEntity order) {
        ProductEntity product = getProductWithUpdateInventory(productEntity, itemRequest.getQuantity());
        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(product.getId());
        item.setName(product.getName());
        item.setPrice(product.getPrice());
        item.setQuantity(itemRequest.getQuantity());
        item.setWeight(product.getWeight());
        order.addItem(item);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.setStatus(newStatus);
        updateTheInventoryAfterReject(order.getItems());
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

    public void updateTheInventoryAfterReject(List<OrderItemEntity> orderItemEntities) {
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            Optional<ProductEntity> productEntity = productRepository.findById(orderItemEntity.getProductId());
            if (productEntity.isPresent()) {
                ProductEntity product = productEntity.get();
                product.setCurrentStock(product.getCurrentStock() + orderItemEntity.getQuantity());
            }
        }
    }

    public Optional<ProductEntity> getProductByNameAndId(String name, Long id) {
        return productRepository.findByNameAndId(name, id);
    }

    // note : shipping offers are available for courier type only
    public BigDecimal getShippingPriceByWeight(Double weight, String deliveryType,
                                                    String district, boolean isDeliveryFreeItemAvailable) {
        RateTypes rateType = isDeliveryFreeItemAvailable && DeliveryTypes.courier.name().equals(deliveryType.toLowerCase())
                ? RateTypes.offer
                : (SPECIAL_RATE_DISTRICTS.contains(district.toLowerCase())
                ? RateTypes.special
                : RateTypes.normal);
        return getShippingPriceByWeight(rateType, deliveryTypeFromString(deliveryType), weight);
    }

//    @Cacheable(
//            value = "shippingRatePrice",
//            key = "#rateType.name() + '_' + #deliveryType.name() + '_' + #weight.toPlainString()"
//    )
   public BigDecimal getShippingPriceByWeight(RateTypes rateType,
                                              DeliveryTypes deliveryType,
                                              Double weight) {
             return shippingRateTiersRepository.findPrice(rateType, deliveryType, weight)
                    .orElseThrow(() -> new IllegalArgumentException("Shipping rate not found"));
   }
}
