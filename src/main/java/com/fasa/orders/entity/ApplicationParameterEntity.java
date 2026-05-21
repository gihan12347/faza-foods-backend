package com.fasa.orders.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Key/value settings (e.g. bank details). Table name matches {@code application_parameter}.
 */
@Entity
@Table(name = "application_parameter")
public class ApplicationParameterEntity {

    @Id
    @Column(name = "param_key", length = 128, nullable = false)
    private String paramKey;

    @Column(name = "param_value", columnDefinition = "TEXT")
    private String paramValue;

    public String getParamKey() {
        return paramKey;
    }

    public void setParamKey(String paramKey) {
        this.paramKey = paramKey;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }
}
