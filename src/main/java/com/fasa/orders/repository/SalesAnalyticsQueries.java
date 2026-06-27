package com.fasa.orders.repository;

import com.fasa.orders.dto.analytics.DailySalesRow;
import com.fasa.orders.dto.analytics.DeliveryTypeSalesRow;
import com.fasa.orders.dto.analytics.DistrictSalesRow;
import com.fasa.orders.dto.analytics.ProductSalesRow;
import com.fasa.orders.dto.analytics.SalesAnalyticsSummary;
import com.fasa.orders.dto.analytics.StatusCountRow;
import com.fasa.orders.entity.OrderStatus;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SalesAnalyticsQueries {

    private final EntityManager entityManager;

    public SalesAnalyticsQueries(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public SalesAnalyticsSummary fetchSummary(LocalDateTime from, LocalDateTime toExclusive) {
        SalesAnalyticsSummary summary = new SalesAnalyticsSummary();

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(o.id), "
                        + "COALESCE(SUM(COALESCE(o.order_price, 0) + COALESCE(o.delivery_price, 0)), 0), "
                        + "COALESCE(SUM(o.order_price), 0), "
                        + "COALESCE(SUM(o.delivery_price), 0) "
                        + "FROM orders o WHERE o.status <> 'REJECT' ");
        Map<String, Object> params = new HashMap<String, Object>();
        appendDateFilter(sql, params, from, toExclusive, "o");

        Object[] row = (Object[]) createNativeQuery(sql.toString(), params).getSingleResult();
        summary.setOrderCount(toLong(row[0]));
        summary.setTotalRevenue(toBigDecimal(row[1]));
        summary.setProductRevenue(toBigDecimal(row[2]));
        summary.setDeliveryRevenue(toBigDecimal(row[3]));

        summary.setItemsSold(fetchItemsSold(from, toExclusive));
        summary.setUniqueProductsSold(fetchUniqueProductsSold(from, toExclusive));
        return summary;
    }

    public List<ProductSalesRow> fetchProductSales(
            LocalDateTime from,
            LocalDateTime toExclusive,
            boolean ascending) {
        StringBuilder sql = new StringBuilder(
                "SELECT oi.product_id, MAX(oi.name), COALESCE(SUM(oi.quantity), 0), "
                        + "COALESCE(SUM(oi.price * oi.quantity), 0) "
                        + "FROM order_items oi "
                        + "INNER JOIN orders o ON o.id = oi.order_id "
                        + "WHERE o.status <> 'REJECT' ");
        Map<String, Object> params = new HashMap<String, Object>();
        appendDateFilter(sql, params, from, toExclusive, "o");
        sql.append(" GROUP BY oi.product_id ");
        sql.append(ascending ? " ORDER BY SUM(oi.quantity) ASC " : " ORDER BY SUM(oi.quantity) DESC ");

        Query query = createNativeQuery(sql.toString(), params);
        List<?> rows = query.getResultList();
        List<ProductSalesRow> result = new ArrayList<ProductSalesRow>();
        for (Object raw : rows) {
            Object[] row = (Object[]) raw;
            Long productId = row[0] != null ? ((Number) row[0]).longValue() : null;
            String name = row[1] != null ? String.valueOf(row[1]) : "Unknown";
            long qty = toLong(row[2]);
            BigDecimal revenue = toBigDecimal(row[3]);
            result.add(new ProductSalesRow(productId, name, qty, revenue));
        }
        return result;
    }

    public List<DailySalesRow> fetchDailySales(LocalDateTime from, LocalDateTime toExclusive) {
        StringBuilder sql = new StringBuilder(
                "SELECT DATE(o.created_at), COUNT(o.id), "
                        + "COALESCE(SUM(COALESCE(o.order_price, 0) + COALESCE(o.delivery_price, 0)), 0) "
                        + "FROM orders o WHERE o.status <> 'REJECT' ");
        Map<String, Object> params = new HashMap<String, Object>();
        appendDateFilter(sql, params, from, toExclusive, "o");
        sql.append(" GROUP BY DATE(o.created_at) ORDER BY DATE(o.created_at) ASC ");

        Query query = createNativeQuery(sql.toString(), params);
        List<?> rows = query.getResultList();
        Map<LocalDate, Long> itemsByDay = fetchItemsSoldByDay(from, toExclusive);
        List<DailySalesRow> result = new ArrayList<DailySalesRow>();
        for (Object raw : rows) {
            Object[] row = (Object[]) raw;
            LocalDate date = toLocalDate(row[0]);
            long orderCount = toLong(row[1]);
            BigDecimal revenue = toBigDecimal(row[2]);
            long itemsSold = date != null && itemsByDay.containsKey(date)
                    ? itemsByDay.get(date).longValue()
                    : 0L;
            result.add(new DailySalesRow(date, orderCount, itemsSold, revenue));
        }
        return result;
    }

    private Map<LocalDate, Long> fetchItemsSoldByDay(LocalDateTime from, LocalDateTime toExclusive) {
        StringBuilder sql = new StringBuilder(
                "SELECT DATE(o.created_at), COALESCE(SUM(oi.quantity), 0) "
                        + "FROM order_items oi "
                        + "INNER JOIN orders o ON o.id = oi.order_id "
                        + "WHERE o.status <> 'REJECT' ");
        Map<String, Object> params = new HashMap<String, Object>();
        appendDateFilter(sql, params, from, toExclusive, "o");
        sql.append(" GROUP BY DATE(o.created_at) ");

        Query query = createNativeQuery(sql.toString(), params);
        List<?> rows = query.getResultList();
        Map<LocalDate, Long> result = new HashMap<LocalDate, Long>();
        for (Object raw : rows) {
            Object[] row = (Object[]) raw;
            LocalDate date = toLocalDate(row[0]);
            if (date != null) {
                result.put(date, Long.valueOf(toLong(row[1])));
            }
        }
        return result;
    }

    public List<StatusCountRow> fetchStatusCounts(LocalDateTime from, LocalDateTime toExclusive) {
        StringBuilder sql = new StringBuilder(
                "SELECT o.status, COUNT(o.id) FROM orders o WHERE 1 = 1 ");
        Map<String, Object> params = new HashMap<String, Object>();
        appendDateFilter(sql, params, from, toExclusive, "o");
        sql.append(" GROUP BY o.status ORDER BY COUNT(o.id) DESC ");

        Query query = createNativeQuery(sql.toString(), params);
        List<?> rows = query.getResultList();
        List<StatusCountRow> result = new ArrayList<StatusCountRow>();
        for (Object raw : rows) {
            Object[] row = (Object[]) raw;
            String status = row[0] != null ? String.valueOf(row[0]) : "UNKNOWN";
            long count = toLong(row[1]);
            result.add(new StatusCountRow(status, resolveStatusLabel(status), count));
        }
        return result;
    }

    public List<DistrictSalesRow> fetchTopDistricts(LocalDateTime from, LocalDateTime toExclusive, int limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(NULLIF(TRIM(o.district), ''), 'Unknown'), COUNT(o.id), "
                        + "COALESCE(SUM(COALESCE(o.order_price, 0) + COALESCE(o.delivery_price, 0)), 0) "
                        + "FROM orders o WHERE o.status <> 'REJECT' ");
        Map<String, Object> params = new HashMap<String, Object>();
        appendDateFilter(sql, params, from, toExclusive, "o");
        sql.append(" GROUP BY COALESCE(NULLIF(TRIM(o.district), ''), 'Unknown') ");
        sql.append(" ORDER BY SUM(COALESCE(o.order_price, 0) + COALESCE(o.delivery_price, 0)) DESC ");
        sql.append(" LIMIT ").append(Math.max(1, limit));

        return mapDistrictRows(createNativeQuery(sql.toString(), params).getResultList());
    }

    public List<DeliveryTypeSalesRow> fetchDeliveryTypes(LocalDateTime from, LocalDateTime toExclusive) {
        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(NULLIF(TRIM(o.delivery_type), ''), 'Unknown'), COUNT(o.id), "
                        + "COALESCE(SUM(COALESCE(o.order_price, 0) + COALESCE(o.delivery_price, 0)), 0) "
                        + "FROM orders o WHERE o.status <> 'REJECT' ");
        Map<String, Object> params = new HashMap<String, Object>();
        appendDateFilter(sql, params, from, toExclusive, "o");
        sql.append(" GROUP BY COALESCE(NULLIF(TRIM(o.delivery_type), ''), 'Unknown') ");
        sql.append(" ORDER BY COUNT(o.id) DESC ");

        Query query = createNativeQuery(sql.toString(), params);
        List<?> rows = query.getResultList();
        List<DeliveryTypeSalesRow> result = new ArrayList<DeliveryTypeSalesRow>();
        for (Object raw : rows) {
            Object[] row = (Object[]) raw;
            String type = row[0] != null ? String.valueOf(row[0]) : "Unknown";
            long count = toLong(row[1]);
            BigDecimal revenue = toBigDecimal(row[2]);
            result.add(new DeliveryTypeSalesRow(formatDeliveryTypeLabel(type), count, revenue));
        }
        return result;
    }

    private long fetchItemsSold(LocalDateTime from, LocalDateTime toExclusive) {
        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(SUM(oi.quantity), 0) FROM order_items oi "
                        + "INNER JOIN orders o ON o.id = oi.order_id "
                        + "WHERE o.status <> 'REJECT' ");
        Map<String, Object> params = new HashMap<String, Object>();
        appendDateFilter(sql, params, from, toExclusive, "o");
        Object value = createNativeQuery(sql.toString(), params).getSingleResult();
        return toLong(value);
    }

    private long fetchUniqueProductsSold(LocalDateTime from, LocalDateTime toExclusive) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT oi.product_id) FROM order_items oi "
                        + "INNER JOIN orders o ON o.id = oi.order_id "
                        + "WHERE o.status <> 'REJECT' ");
        Map<String, Object> params = new HashMap<String, Object>();
        appendDateFilter(sql, params, from, toExclusive, "o");
        Object value = createNativeQuery(sql.toString(), params).getSingleResult();
        return toLong(value);
    }

    private List<DistrictSalesRow> mapDistrictRows(List<?> rows) {
        List<DistrictSalesRow> result = new ArrayList<DistrictSalesRow>();
        for (Object raw : rows) {
            Object[] row = (Object[]) raw;
            String district = row[0] != null ? String.valueOf(row[0]) : "Unknown";
            long count = toLong(row[1]);
            BigDecimal revenue = toBigDecimal(row[2]);
            result.add(new DistrictSalesRow(district, count, revenue));
        }
        return result;
    }

    private Query createNativeQuery(String sql, Map<String, Object> params) {
        Query query = entityManager.createNativeQuery(sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query;
    }

    private static void appendDateFilter(
            StringBuilder sql,
            Map<String, Object> params,
            LocalDateTime from,
            LocalDateTime toExclusive,
            String alias) {
        if (from != null) {
            sql.append(" AND ").append(alias).append(".created_at >= :from ");
            params.put("from", from);
        }
        if (toExclusive != null) {
            sql.append(" AND ").append(alias).append(".created_at < :to ");
            params.put("to", toExclusive);
        }
    }

    private static long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return new BigDecimal(String.valueOf(value));
    }

    private static LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof Date) {
            return ((Date) value).toLocalDate();
        }
        if (value instanceof java.util.Date) {
            return new Date(((java.util.Date) value).getTime()).toLocalDate();
        }
        return LocalDate.parse(String.valueOf(value));
    }

    private static String resolveStatusLabel(String status) {
        try {
            return OrderStatus.valueOf(status).getLabel();
        } catch (IllegalArgumentException ex) {
            return status;
        }
    }

    private static String formatDeliveryTypeLabel(String raw) {
        if (raw == null || raw.trim().isEmpty() || "Unknown".equalsIgnoreCase(raw)) {
            return "Unknown";
        }
        String normalized = raw.trim().toLowerCase();
        if ("courier".equals(normalized)) {
            return "Courier";
        }
        if ("pickup".equals(normalized)) {
            return "Pickup";
        }
        return raw.substring(0, 1).toUpperCase() + raw.substring(1);
    }
}
