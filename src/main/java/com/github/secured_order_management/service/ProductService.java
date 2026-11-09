package com.github.secured_order_management.service;

import com.github.secured_order_management.dto.ProductRequest;
import com.github.secured_order_management.dto.ProductResponse;
import com.github.secured_order_management.entity.Product;
import com.github.secured_order_management.entity.User;
import com.github.secured_order_management.repository.ProductRepository;
import com.github.secured_order_management.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<ProductResponse> getProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ProductResponse getProduct(Long id) {
        return ProductResponse.from(findProduct(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ProductResponse createProduct(ProductRequest request, String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        Product product = new Product();
        product.setName(request.name().trim());
        product.setPrice(request.price());
        product.setUserId(user.getId());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productAuthorization.isOwner(#p0, authentication.name))")
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProduct(id);
        product.setName(request.name().trim());
        product.setPrice(request.price());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productAuthorization.isOwner(#p0, authentication.name))")
    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        productRepository.delete(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
}
