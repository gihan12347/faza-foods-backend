package com.fasa.orders.controller;

import com.fasa.orders.dto.analytics.SalesAnalyticsDashboard;
import com.fasa.orders.service.SalesAnalyticsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Map;

@Controller
public class DashboardAnalyticsController {

    private final SalesAnalyticsService salesAnalyticsService;
    private final ObjectMapper objectMapper;

    public DashboardAnalyticsController(SalesAnalyticsService salesAnalyticsService, ObjectMapper objectMapper) {
        this.salesAnalyticsService = salesAnalyticsService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/analytics")
    public String analytics(
            @RequestParam(defaultValue = "30d") String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) throws JsonProcessingException {

        SalesAnalyticsDashboard dashboard = salesAnalyticsService.buildDashboard(range, from, to);
        Map<String, Object> chartPayload = salesAnalyticsService.toChartPayload(dashboard);

        model.addAttribute("analytics", dashboard);
        model.addAttribute("sidebarActive", "analytics");
        model.addAttribute("chartJson", objectMapper.writeValueAsString(chartPayload));
        model.addAttribute("selectedRange", dashboard.getRangeKey());
        model.addAttribute("customFrom", from);
        model.addAttribute("customTo", to);

        return "dashboard-analytics";
    }
}
