package com.fasa.orders.config;

import com.fasa.orders.dto.LowStockNotificationDto;
import com.fasa.orders.service.ProductService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

/**
 * Exposes low-stock notifications on authenticated admin pages.
 */
@ControllerAdvice
public class StockNotificationAdvice {

    private final ProductService productService;

    public StockNotificationAdvice(ProductService productService) {
        this.productService = productService;
    }

    @ModelAttribute
    public void addLowStockNotifications(Model model) {
        if (!isAuthenticatedUser()) {
            model.addAttribute("lowStockNotifications", Collections.emptyList());
            model.addAttribute("lowStockNotificationCount", 0);
            return;
        }
        List<LowStockNotificationDto> notifications = productService.findLowStockNotifications();
        model.addAttribute("lowStockNotifications", notifications);
        model.addAttribute("lowStockNotificationCount", notifications.size());
    }

    private static boolean isAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }
}
