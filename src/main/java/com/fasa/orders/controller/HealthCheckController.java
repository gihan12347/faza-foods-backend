package com.fasa.orders.controller;

import com.fasa.orders.dto.OrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<OrderResponse> health() {
        return ResponseEntity.ok(new OrderResponse("OK", "Order API is running"));
    }
}
