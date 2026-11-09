package com.github.secured_order_management.service;

import com.github.secured_order_management.dto.ProductRequest;
import com.github.secured_order_management.entity.Product;
import com.github.secured_order_management.entity.Role;
import com.github.secured_order_management.entity.User;
import com.github.secured_order_management.repository.ProductRepository;
import com.github.secured_order_management.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ProductServiceMethodSecurityTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser(username = "seller", roles = "SELLER")
    void sellerCannotUpdateAnotherOwnersProduct() {
        Product product = createProductFor("another-seller");

        assertThrows(AccessDeniedException.class, () -> productService.updateProduct(
                product.getId(),
                new ProductRequest("Updated", 12.0)
        ));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanUpdateAnotherOwnersProduct() {
        Product product = createProductFor("another-seller");

        productService.updateProduct(product.getId(), new ProductRequest("Updated", 12.0));

        assertEquals("Updated", productRepository.findById(product.getId()).orElseThrow().getName());
    }

    private Product createProductFor(String username) {
        User owner = new User();
        owner.setUsername(username);
        owner.setName(username);
        owner.setPassword("encoded-password");
        owner.setRole(Role.SELLER);
        owner = userRepository.saveAndFlush(owner);

        Product product = new Product();
        product.setName("Original");
        product.setPrice(10.0);
        product.setUserId(owner.getId());
        return productRepository.saveAndFlush(product);
    }

}
