package com.fasa.orders.dto.analytics;

public class StatusCountRow {

    private String status;
    private String label;
    private long count;

    public StatusCountRow() {
    }

    public StatusCountRow(String status, String label, long count) {
        this.status = status;
        this.label = label;
        this.count = count;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
