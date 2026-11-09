package com.github.secured_order_management.dto;

import com.github.secured_order_management.entity.Role;
import com.github.secured_order_management.entity.User;

import java.time.OffsetDateTime;

public record UserResponse(Long id, String username, String name, Role role, OffsetDateTime createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
