package com.fasa.orders.dto.analytics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesAnalyticsDashboard {

    private String rangeKey;
    private String rangeLabel;
    private LocalDate fromDate;
    private LocalDate toDate;
    private SalesAnalyticsSummary summary = new SalesAnalyticsSummary();
    private List<ProductSalesRow> topProducts = new ArrayList<ProductSalesRow>();
    private List<ProductSalesRow> lowestProducts = new ArrayList<ProductSalesRow>();
    private List<DailySalesRow> dailySales = new ArrayList<DailySalesRow>();
    private List<StatusCountRow> statusCounts = new ArrayList<StatusCountRow>();
    private List<DistrictSalesRow> topDistricts = new ArrayList<DistrictSalesRow>();
    private List<DeliveryTypeSalesRow> deliveryTypes = new ArrayList<DeliveryTypeSalesRow>();

    public String getRangeKey() {
        return rangeKey;
    }

    public void setRangeKey(String rangeKey) {
        this.rangeKey = rangeKey;
    }

    public String getRangeLabel() {
        return rangeLabel;
    }

    public void setRangeLabel(String rangeLabel) {
        this.rangeLabel = rangeLabel;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public SalesAnalyticsSummary getSummary() {
        return summary;
    }

    public void setSummary(SalesAnalyticsSummary summary) {
        this.summary = summary != null ? summary : new SalesAnalyticsSummary();
    }

    public List<ProductSalesRow> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<ProductSalesRow> topProducts) {
        this.topProducts = topProducts != null ? topProducts : new ArrayList<ProductSalesRow>();
    }

    public List<ProductSalesRow> getLowestProducts() {
        return lowestProducts;
    }

    public void setLowestProducts(List<ProductSalesRow> lowestProducts) {
        this.lowestProducts = lowestProducts != null ? lowestProducts : new ArrayList<ProductSalesRow>();
    }

    public List<DailySalesRow> getDailySales() {
        return dailySales;
    }

    public void setDailySales(List<DailySalesRow> dailySales) {
        this.dailySales = dailySales != null ? dailySales : new ArrayList<DailySalesRow>();
    }

    public List<StatusCountRow> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(List<StatusCountRow> statusCounts) {
        this.statusCounts = statusCounts != null ? statusCounts : new ArrayList<StatusCountRow>();
    }

    public List<DistrictSalesRow> getTopDistricts() {
        return topDistricts;
    }

    public void setTopDistricts(List<DistrictSalesRow> topDistricts) {
        this.topDistricts = topDistricts != null ? topDistricts : new ArrayList<DistrictSalesRow>();
    }

    public List<DeliveryTypeSalesRow> getDeliveryTypes() {
        return deliveryTypes;
    }

    public void setDeliveryTypes(List<DeliveryTypeSalesRow> deliveryTypes) {
        this.deliveryTypes = deliveryTypes != null ? deliveryTypes : new ArrayList<DeliveryTypeSalesRow>();
    }
}
