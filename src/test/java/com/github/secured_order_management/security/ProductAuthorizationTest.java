package com.github.secured_order_management.security;

import com.github.secured_order_management.entity.User;
import com.github.secured_order_management.repository.ProductRepository;
import com.github.secured_order_management.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductAuthorizationTest {
    @Test
    void allowsOwnerAndRejectsNonOwner() {
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ProductAuthorization authorization = new ProductAuthorization(productRepository, userRepository);
        User user = new User();
        user.setId(7L);
        when(userRepository.findByUsernameIgnoreCase("seller")).thenReturn(Optional.of(user));
        when(productRepository.existsByIdAndUserId(3L, 7L)).thenReturn(true);
        when(productRepository.existsByIdAndUserId(4L, 7L)).thenReturn(false);

        assertTrue(authorization.isOwner(3L, "seller"));
        assertFalse(authorization.isOwner(4L, "seller"));
    }

    @Test
    void rejectsUnknownUser() {
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ProductAuthorization authorization = new ProductAuthorization(productRepository, userRepository);
        when(userRepository.findByUsernameIgnoreCase("unknown")).thenReturn(Optional.empty());

        assertFalse(authorization.isOwner(3L, "unknown"));
    }
}
