package com.fasa.orders.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class DeliveryDetailsRequest {

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 120, message = "Customer name must be between 2 and 120 characters")
    private String customerName;

    private String deliveryType;
    private String addressLine1;
    private String addressLine2;
    private String district;
    private String whatsappNumber;
    private String otherPhoneNumber;

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWhatsappNumber() {
        return whatsappNumber;
    }

    public void setWhatsappNumber(String whatsappNumber) {
        this.whatsappNumber = whatsappNumber;
    }

    public String getOtherPhoneNumber() {
        return otherPhoneNumber;
    }

    public void setOtherPhoneNumber(String otherPhoneNumber) {
        this.otherPhoneNumber = otherPhoneNumber;
    }
}
