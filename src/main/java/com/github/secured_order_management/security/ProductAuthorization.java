package com.github.secured_order_management.security;

import com.github.secured_order_management.entity.User;
import com.github.secured_order_management.repository.ProductRepository;
import com.github.secured_order_management.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class ProductAuthorization {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductAuthorization(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public boolean isOwner(Long productId, String username) {
        User user = userRepository.findByUsernameIgnoreCase(username).orElse(null);
        return user != null
                && productRepository.existsByIdAndUserId(productId, user.getId());
    }
}
