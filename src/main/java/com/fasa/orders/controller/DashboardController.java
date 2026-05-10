package com.fasa.orders.controller;

import com.fasa.orders.entity.OrderEntity;
import com.fasa.orders.entity.OrderStatus;
import com.fasa.orders.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
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
    private final String courierProcessingBankWhatsAppBlock;
    private final boolean dashboardWhatsappPromptOnDelivered;

    public DashboardController(
            OrderService orderService,
            @Value("${fasa.orders.bank.account-name:I.F. Fasana}") String bankAccountName,
            @Value("${fasa.orders.bank.account-number:94089358}") String bankAccountNumber,
            @Value("${fasa.orders.bank.bank-label:Bank of Ceylon (BOC)}") String bankLabel,
            @Value("${fasa.orders.bank.branch:Ibbagamuwa}") String bankBranch,
            @Value("${fasa.dashboard.whatsapp.prompt-on-delivered:true}") boolean dashboardWhatsappPromptOnDelivered) {
        this.orderService = orderService;
        this.courierProcessingBankWhatsAppBlock =
                buildCourierProcessingBankWhatsAppBlock(bankAccountName, bankAccountNumber, bankLabel, bankBranch);
        this.dashboardWhatsappPromptOnDelivered = dashboardWhatsappPromptOnDelivered;
    }

    private static String buildCourierProcessingBankWhatsAppBlock(
            String accountName, String accountNumber, String bankLabel, String branch) {
        String name = trimToEmpty(accountName);
        String number = trimToEmpty(accountNumber);
        String bank = trimToEmpty(bankLabel);
        String br = trimToEmpty(branch);
        StringBuilder sb = new StringBuilder(320);
        sb.append("---\n");
        sb.append("Payment (courier — bank transfer)\n\n");
        sb.append("Account name: ").append(name).append('\n');
        sb.append("Account number: ").append(number).append('\n');
        sb.append("Bank: ").append(bank).append('\n');
        sb.append("Branch: ").append(br).append('\n');
        sb.append('\n');
        sb.append("Please transfer the total amount shown above. Use your Order ID as the payment reference ");
        sb.append("if your bank allows a remark or note.\n");
        sb.append("---");
        return sb.toString();
    }

    private static String trimToEmpty(String s) {
        return s == null ? "" : s.trim();
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
        model.addAttribute("courierProcessingBankWhatsAppBlock", courierProcessingBankWhatsAppBlock);
        model.addAttribute("dashboardWhatsappPromptOnDelivered", dashboardWhatsappPromptOnDelivered);

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
            if (s == OrderStatus.DONE) {
                return "done";
            }
            if (s == OrderStatus.REJECT) {
                return "reject";
            }
        } catch (IllegalArgumentException ignored) {
            // legacy DB value e.g. COMPLETED — treat as no sidebar match
        }
        return "all";
    }
}
