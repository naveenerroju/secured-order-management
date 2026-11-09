package com.github.secured_order_management.dto;

import com.github.secured_order_management.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Size(max = 64)
        @Pattern(regexp = "[A-Za-z0-9._-]+")
        String username,
        @NotBlank String name,
        @NotBlank String password,
        @NotNull Role role
) {
}
