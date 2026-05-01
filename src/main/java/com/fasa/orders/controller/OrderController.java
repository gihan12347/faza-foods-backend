package com.fasa.orders.controller;

import com.fasa.orders.dto.OrderRequest;
import com.fasa.orders.dto.OrderResponse;
import com.fasa.orders.entity.OrderEntity;
import com.fasa.orders.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<OrderResponse> health() {
        return ResponseEntity.ok(new OrderResponse("OK", "Order API is running"));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderEntity savedOrder = orderService.saveOrder(request);
        String message = "Order submitted successfully. Order ID: " + savedOrder.getId();
        return ResponseEntity.ok(new OrderResponse("SUCCESS", message));
    }
}
