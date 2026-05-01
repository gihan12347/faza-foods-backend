package com.fasa.orders.controller;

import com.fasa.orders.entity.OrderEntity;
import com.fasa.orders.entity.OrderStatus;
import com.fasa.orders.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Controller
public class DashboardController {

    private final OrderService orderService;

    public DashboardController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Model model) {

        Page<OrderEntity> ordersPage = orderService.findOrdersPage(search, status, page, size);

        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("currentPage", ordersPage.getNumber());
        model.addAttribute("pageSize", ordersPage.getSize());
        model.addAttribute("search", search != null ? search : "");
        String statusValue = status != null ? status : "";
        model.addAttribute("statusFilter", statusValue);
        model.addAttribute("sidebarActive", resolveSidebarActive(status));
        model.addAttribute("orderStatuses", OrderStatus.values());

        int totalPages = ordersPage.getTotalPages();
        int current = ordersPage.getNumber();
        int pageFrom = 0;
        int pageTo = 0;
        if (totalPages > 0) {
            pageFrom = Math.max(0, current - 2);
            pageTo = Math.min(totalPages - 1, current + 2);
        }
        model.addAttribute("pageFrom", pageFrom);
        model.addAttribute("pageTo", pageTo);

        return "dashboard";
    }

    @PostMapping("/dashboard/orders/{id}/status")
    public String updateOrderStatus(
            @PathVariable("id") Long orderId,
            @RequestParam("newStatus") String newStatusRaw,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(value = "filterStatus", required = false) String filterStatus,
            RedirectAttributes redirectAttributes) {

        Optional<OrderStatus> parsed = OrderService.parseStatus(newStatusRaw);
        if (!parsed.isPresent()) {
            redirectAttributes.addFlashAttribute("flashError", "Invalid status selected.");
        } else {
            try {
                orderService.updateOrderStatus(orderId, parsed.get());
                redirectAttributes.addFlashAttribute("flashMessage", "Order status updated.");
            } catch (IllegalArgumentException ex) {
                redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
            }
        }

        UriComponentsBuilder redirectBuilder = UriComponentsBuilder.fromPath("/dashboard")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("search", search != null ? search : "");
        if (StringUtils.hasText(filterStatus)) {
            redirectBuilder.queryParam("status", filterStatus);
        }
        String redirectUrl = redirectBuilder.build().encode().toUriString();

        return "redirect:" + redirectUrl;
    }

    private static String resolveSidebarActive(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "all";
        }
        try {
            OrderStatus s = OrderStatus.valueOf(status.trim().toUpperCase());
            if (s == OrderStatus.PENDING) {
                return "pending";
            }
            if (s == OrderStatus.PROCESSING) {
                return "processing";
            }
            if (s == OrderStatus.DELIVERED) {
                return "delivered";
            }
        } catch (IllegalArgumentException ignored) {
            // legacy DB value e.g. COMPLETED — treat as no sidebar match
        }
        return "all";
    }
}
