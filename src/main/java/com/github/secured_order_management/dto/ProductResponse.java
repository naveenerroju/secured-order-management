package com.github.secured_order_management.dto;

import com.github.secured_order_management.entity.Product;

public record ProductResponse(Long id, String name, Double price, Long userId) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getUserId()
        );
    }
}
