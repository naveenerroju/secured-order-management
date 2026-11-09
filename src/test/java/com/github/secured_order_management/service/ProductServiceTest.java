package com.github.secured_order_management.service;

import com.github.secured_order_management.dto.ProductRequest;
import com.github.secured_order_management.dto.ProductResponse;
import com.github.secured_order_management.entity.Product;
import com.github.secured_order_management.entity.User;
import com.github.secured_order_management.repository.ProductRepository;
import com.github.secured_order_management.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {
    @Test
    void createProductUsesAuthenticatedUserAsOwner() {
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ProductService productService = new ProductService(productRepository, userRepository);
        User owner = new User();
        owner.setId(7L);
        owner.setName("seller");
        when(userRepository.findByUsernameIgnoreCase("seller")).thenReturn(Optional.of(owner));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(3L);
            return product;
        });

        ProductResponse response = productService.createProduct(
                new ProductRequest("  Desk  ", 99.99),
                "seller"
        );

        assertEquals(3L, response.id());
        assertEquals("Desk", response.name());
        assertEquals(99.99, response.price());
        assertEquals(7L, response.userId());
    }

    @Test
    void updateProductChangesOnlyEditableFields() {
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ProductService productService = new ProductService(productRepository, userRepository);
        Product existing = new Product();
        existing.setId(3L);
        existing.setName("Old name");
        existing.setPrice(10.0);
        existing.setUserId(7L);
        when(productRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        ProductResponse response = productService.updateProduct(
                3L,
                new ProductRequest("New name", 20.0)
        );

        assertEquals("New name", response.name());
        assertEquals(20.0, response.price());
        assertEquals(7L, response.userId());
        verify(productRepository).save(existing);
    }
}
