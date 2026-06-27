package com.fasa.orders.service;

import com.fasa.orders.dto.analytics.DailySalesRow;
import com.fasa.orders.dto.analytics.ProductSalesRow;
import com.fasa.orders.dto.analytics.SalesAnalyticsDashboard;
import com.fasa.orders.dto.analytics.SalesAnalyticsSummary;
import com.fasa.orders.repository.SalesAnalyticsQueries;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalesAnalyticsService {

    private static final int TOP_PRODUCT_LIMIT = 10;
    private static final int TOP_DISTRICT_LIMIT = 8;

    private final SalesAnalyticsQueries salesAnalyticsQueries;

    public SalesAnalyticsService(SalesAnalyticsQueries salesAnalyticsQueries) {
        this.salesAnalyticsQueries = salesAnalyticsQueries;
    }

    @Transactional(readOnly = true)
    public SalesAnalyticsDashboard buildDashboard(String rangeKey, LocalDate from, LocalDate to) {
        AnalyticsDateRange range = resolveRange(rangeKey, from, to);

        SalesAnalyticsDashboard dashboard = new SalesAnalyticsDashboard();
        dashboard.setRangeKey(range.getKey());
        dashboard.setRangeLabel(range.getLabel());
        dashboard.setFromDate(range.getFromDate());
        dashboard.setToDate(range.getToDate());

        SalesAnalyticsSummary summary = salesAnalyticsQueries.fetchSummary(range.getFrom(), range.getToExclusive());
        dashboard.setSummary(summary);

        List<ProductSalesRow> allProducts = salesAnalyticsQueries.fetchProductSales(
                range.getFrom(), range.getToExclusive(), false);
        applyProductShares(allProducts, summary.getProductRevenue());

        List<ProductSalesRow> topProducts = new ArrayList<ProductSalesRow>();
        for (int i = 0; i < allProducts.size() && i < TOP_PRODUCT_LIMIT; i++) {
            topProducts.add(allProducts.get(i));
        }
        dashboard.setTopProducts(topProducts);

        List<ProductSalesRow> lowestSource = salesAnalyticsQueries.fetchProductSales(
                range.getFrom(), range.getToExclusive(), true);
        applyProductShares(lowestSource, summary.getProductRevenue());
        List<ProductSalesRow> lowestProducts = new ArrayList<ProductSalesRow>();
        for (int i = 0; i < lowestSource.size() && i < TOP_PRODUCT_LIMIT; i++) {
            lowestProducts.add(lowestSource.get(i));
        }
        dashboard.setLowestProducts(lowestProducts);

        List<DailySalesRow> dailySales = salesAnalyticsQueries.fetchDailySales(range.getFrom(), range.getToExclusive());
        dashboard.setDailySales(fillDailyGaps(dailySales, range.getFromDate(), range.getToDate()));

        dashboard.setStatusCounts(salesAnalyticsQueries.fetchStatusCounts(range.getFrom(), range.getToExclusive()));
        dashboard.setTopDistricts(salesAnalyticsQueries.fetchTopDistricts(
                range.getFrom(), range.getToExclusive(), TOP_DISTRICT_LIMIT));
        dashboard.setDeliveryTypes(salesAnalyticsQueries.fetchDeliveryTypes(range.getFrom(), range.getToExclusive()));

        return dashboard;
    }

    public Map<String, Object> toChartPayload(SalesAnalyticsDashboard dashboard) {
        Map<String, Object> payload = new HashMap<String, Object>();

        List<String> dailyLabels = new ArrayList<String>();
        List<Double> dailyRevenue = new ArrayList<Double>();
        List<Long> dailyOrders = new ArrayList<Long>();
        List<Long> dailyItems = new ArrayList<Long>();
        for (DailySalesRow row : dashboard.getDailySales()) {
            dailyLabels.add(row.getDate() != null ? row.getDate().toString() : "");
            dailyRevenue.add(row.getRevenue().doubleValue());
            dailyOrders.add(row.getOrderCount());
            dailyItems.add(row.getItemsSold());
        }
        payload.put("dailyLabels", dailyLabels);
        payload.put("dailyRevenue", dailyRevenue);
        payload.put("dailyOrders", dailyOrders);
        payload.put("dailyItems", dailyItems);

        payload.put("topProductLabels", labelsFromProducts(dashboard.getTopProducts()));
        payload.put("topProductQty", quantitiesFromProducts(dashboard.getTopProducts()));
        payload.put("topProductRevenue", revenuesFromProducts(dashboard.getTopProducts()));

        payload.put("lowProductLabels", labelsFromProducts(dashboard.getLowestProducts()));
        payload.put("lowProductQty", quantitiesFromProducts(dashboard.getLowestProducts()));
        payload.put("lowProductRevenue", revenuesFromProducts(dashboard.getLowestProducts()));

        List<String> statusLabels = new ArrayList<String>();
        List<Long> statusCounts = new ArrayList<Long>();
        for (com.fasa.orders.dto.analytics.StatusCountRow row : dashboard.getStatusCounts()) {
            statusLabels.add(row.getLabel());
            statusCounts.add(row.getCount());
        }
        payload.put("statusLabels", statusLabels);
        payload.put("statusCounts", statusCounts);

        List<String> districtLabels = new ArrayList<String>();
        List<Double> districtRevenue = new ArrayList<Double>();
        for (com.fasa.orders.dto.analytics.DistrictSalesRow row : dashboard.getTopDistricts()) {
            districtLabels.add(row.getDistrict());
            districtRevenue.add(row.getRevenue().doubleValue());
        }
        payload.put("districtLabels", districtLabels);
        payload.put("districtRevenue", districtRevenue);

        List<String> deliveryLabels = new ArrayList<String>();
        List<Long> deliveryCounts = new ArrayList<Long>();
        for (com.fasa.orders.dto.analytics.DeliveryTypeSalesRow row : dashboard.getDeliveryTypes()) {
            deliveryLabels.add(row.getDeliveryType());
            deliveryCounts.add(row.getOrderCount());
        }
        payload.put("deliveryLabels", deliveryLabels);
        payload.put("deliveryCounts", deliveryCounts);

        SalesAnalyticsSummary summary = dashboard.getSummary();
        payload.put("revenueSplit", new double[] {
                summary.getProductRevenue().doubleValue(),
                summary.getDeliveryRevenue().doubleValue()
        });

        return payload;
    }

    private static void applyProductShares(List<ProductSalesRow> products, BigDecimal totalProductRevenue) {
        for (ProductSalesRow row : products) {
            row.applySharePercent(totalProductRevenue);
        }
    }

    private static List<String> labelsFromProducts(List<ProductSalesRow> products) {
        List<String> labels = new ArrayList<String>();
        for (ProductSalesRow row : products) {
            String name = row.getName() != null ? row.getName() : "Product";
            if (name.length() > 28) {
                name = name.substring(0, 25) + "...";
            }
            labels.add(name);
        }
        return labels;
    }

    private static List<Long> quantitiesFromProducts(List<ProductSalesRow> products) {
        List<Long> values = new ArrayList<Long>();
        for (ProductSalesRow row : products) {
            values.add(row.getQuantitySold());
        }
        return values;
    }

    private static List<Double> revenuesFromProducts(List<ProductSalesRow> products) {
        List<Double> values = new ArrayList<Double>();
        for (ProductSalesRow row : products) {
            values.add(row.getRevenue().doubleValue());
        }
        return values;
    }

    private static List<DailySalesRow> fillDailyGaps(
            List<DailySalesRow> rows,
            LocalDate fromDate,
            LocalDate toDate) {
        if (fromDate == null || toDate == null || rows == null || rows.isEmpty()) {
            return rows != null ? rows : new ArrayList<DailySalesRow>();
        }
        if (fromDate.isAfter(toDate)) {
            return rows;
        }

        Map<LocalDate, DailySalesRow> byDate = new LinkedHashMap<LocalDate, DailySalesRow>();
        for (DailySalesRow row : rows) {
            if (row.getDate() != null) {
                byDate.put(row.getDate(), row);
            }
        }

        List<DailySalesRow> filled = new ArrayList<DailySalesRow>();
        LocalDate cursor = fromDate;
        while (!cursor.isAfter(toDate)) {
            DailySalesRow existing = byDate.get(cursor);
            if (existing != null) {
                filled.add(existing);
            } else {
                filled.add(new DailySalesRow(cursor, 0, 0, BigDecimal.ZERO));
            }
            cursor = cursor.plusDays(1);
        }
        return filled;
    }

    private static AnalyticsDateRange resolveRange(String rangeKey, LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();
        String key = StringUtils.hasText(rangeKey) ? rangeKey.trim().toLowerCase() : "30d";

        if ("custom".equals(key) && from != null) {
            LocalDate end = to != null ? to : today;
            if (end.isBefore(from)) {
                LocalDate swap = from;
                from = end;
                end = swap;
            }
            return new AnalyticsDateRange(
                    "custom",
                    "Custom range",
                    from,
                    end,
                    from.atStartOfDay(),
                    end.plusDays(1).atStartOfDay());
        }

        if ("7d".equals(key)) {
            LocalDate start = today.minusDays(6);
            return new AnalyticsDateRange(
                    key, "Last 7 days", start, today, start.atStartOfDay(), today.plusDays(1).atStartOfDay());
        }
        if ("90d".equals(key)) {
            LocalDate start = today.minusDays(89);
            return new AnalyticsDateRange(
                    key, "Last 90 days", start, today, start.atStartOfDay(), today.plusDays(1).atStartOfDay());
        }
        if ("365d".equals(key) || "1y".equals(key)) {
            LocalDate start = today.minusDays(364);
            return new AnalyticsDateRange(
                    key, "Last 12 months", start, today, start.atStartOfDay(), today.plusDays(1).atStartOfDay());
        }
        if ("ytd".equals(key)) {
            LocalDate start = LocalDate.of(today.getYear(), 1, 1);
            return new AnalyticsDateRange(
                    key, "Year to date", start, today, start.atStartOfDay(), today.plusDays(1).atStartOfDay());
        }
        if ("all".equals(key)) {
            return new AnalyticsDateRange(key, "All time", null, null, null, null);
        }

        LocalDate start30 = today.minusDays(29);
        return new AnalyticsDateRange(
                "30d",
                "Last 30 days",
                start30,
                today,
                start30.atStartOfDay(),
                today.plusDays(1).atStartOfDay());
    }

    private static final class AnalyticsDateRange {
        private final String key;
        private final String label;
        private final LocalDate fromDate;
        private final LocalDate toDate;
        private final LocalDateTime from;
        private final LocalDateTime toExclusive;

        private AnalyticsDateRange(
                String key,
                String label,
                LocalDate fromDate,
                LocalDate toDate,
                LocalDateTime from,
                LocalDateTime toExclusive) {
            this.key = key;
            this.label = label;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.from = from;
            this.toExclusive = toExclusive;
        }

        private String getKey() {
            return key;
        }

        private String getLabel() {
            return label;
        }

        private LocalDate getFromDate() {
            return fromDate;
        }

        private LocalDate getToDate() {
            return toDate;
        }

        private LocalDateTime getFrom() {
            return from;
        }

        private LocalDateTime getToExclusive() {
            return toExclusive;
        }
    }
}
