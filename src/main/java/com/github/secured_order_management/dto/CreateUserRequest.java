package com.github.secured_order_management.dto;

import com.github.secured_order_management.entity.Role;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank String name,
        @NotNull Role role,
        @NotBlank String password
) {
    @AssertTrue(message = "Role must be CUSTOMER or SELLER")
    public boolean isRoleAssignable() {
        return role == Role.CUSTOMER || role == Role.SELLER;
    }
}
