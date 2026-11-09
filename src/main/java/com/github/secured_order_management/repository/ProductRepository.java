package com.github.secured_order_management.repository;

import com.github.secured_order_management.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByIdAndUserId(Long id, Long userId);
}
