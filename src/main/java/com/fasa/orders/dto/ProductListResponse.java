package com.fasa.orders.dto;

import java.util.ArrayList;
import java.util.List;

public class ProductListResponse {

    private List<ProductDto> products = new ArrayList<ProductDto>();

    public ProductListResponse() {
    }

    public ProductListResponse(List<ProductDto> products) {
        this.products = products != null ? products : new ArrayList<ProductDto>();
    }

    public List<ProductDto> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDto> products) {
        this.products = products != null ? products : new ArrayList<ProductDto>();
    }
}
