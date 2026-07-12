package com.fasa.orders.controller;

import com.fasa.orders.dto.*;
import com.fasa.orders.entity.OrderEntity;
import com.fasa.orders.service.ApplicationParameterService;
import com.fasa.orders.service.OrderReceiptPdfService;
import com.fasa.orders.service.OrderService;
import com.fasa.orders.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.fasa.orders.constants.Constant.*;

@RestController
@Validated
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final OrderReceiptPdfService orderReceiptPdfService;
    private final ApplicationParameterService applicationParameterService;

    public OrderController(
            OrderService orderService,
            OrderReceiptPdfService orderReceiptPdfService, ApplicationParameterService applicationParameterService) {
        this.orderService = orderService;
        this.orderReceiptPdfService = orderReceiptPdfService;
        this.applicationParameterService = applicationParameterService;
    }

    @GetMapping
    public ResponseEntity<OrderResponse> health() {
        return ResponseEntity.ok(new OrderResponse("OK", "Order API is running"));
    }

    @PostMapping("/cart/summary")
    public Map<String, BigDecimal> previewOrderPrice(@Valid @RequestBody OrderRequest request) {

        BigDecimal subTotal = BigDecimal.ZERO;
        double totalWeight = 0.0;
        boolean isDeliveryFreeItemAvailable = false;

        for (OrderItemRequest item : request.getItems()) {
            int quantity = item.getQuantity();
            if (item.isDeliveryFree()) {
                isDeliveryFreeItemAvailable = true;
            }
            subTotal = subTotal.add(
                    item.getPrice().multiply(BigDecimal.valueOf(quantity))
            );
            totalWeight += Utils.parseWeightToKg(item.getWeight()) * quantity;
        }

        DeliveryDetailsRequest delivery = request.getDeliveryDetails();
        BigDecimal shipping = orderService.getShippingPriceByWeight(
                totalWeight,
                delivery.getDeliveryType(),
                delivery.getDistrict(), isDeliveryFreeItemAvailable);

        Map<String, BigDecimal> summary = new LinkedHashMap<>();
        summary.put(SUB_TOTAL, subTotal);
        summary.put(SHIPPING, shipping);
        summary.put(TOTAL, subTotal.add(shipping));

        return summary;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderEntity savedOrder = orderService.saveOrder(request);
        String message = "Order submitted successfully & we will notify you soon. Your Order ID: " + savedOrder.getId();
        OrderResponse body = new OrderResponse("SUCCESS", message);
        body.setOrderId(savedOrder.getId());
        body.setDownloadUrl(applicationParameterService.getStorePublicBaseUrl() + "/download/" + savedOrder.getId());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/public/{token}")
    public ResponseEntity<PublicOrderStatusResponse> getPublicOrderStatus(@PathVariable("token") String token) {
        return orderService.findPublicStatusByOrderToken(token)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/download/{orderId}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long orderId) {
        return orderService.findOrderWithItems(orderId)
                .map(this::buildPdfDownloadResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<byte[]> buildPdfDownloadResponse(OrderEntity order) {
        try {
            byte[] pdf = orderReceiptPdfService.generateReceiptPdf(order);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=fasa-order-" + order.getId() + ".pdf");
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            log.error("PDF generation failed for order {}", order.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
