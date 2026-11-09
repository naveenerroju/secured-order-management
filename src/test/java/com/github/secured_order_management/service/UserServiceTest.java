package com.github.secured_order_management.service;

import com.github.secured_order_management.dto.CreateUserRequest;
import com.github.secured_order_management.dto.UserResponse;
import com.github.secured_order_management.entity.Role;
import com.github.secured_order_management.entity.User;
import com.github.secured_order_management.repository.UserRepository;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import jakarta.validation.Validator;

class UserServiceTest {
    @Test
    void registrationValidationRejectsBlankCredentials() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            assertFalse(validator.validate(
                    new CreateUserRequest("", "Sam", "", null)
            ).isEmpty());
        }
    }

    @Test
    void registerUserHashesPasswordAndReturnsPasswordFreeResponse() {
        UserRepository userRepository = mock(UserRepository.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        UserService userService = new UserService(userRepository, passwordEncoder);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(OffsetDateTime.now());
            return saved;
        });

        UserResponse response = userService.registerUser(
                new CreateUserRequest(
                        "  sammy  ",
                        "  Sam Smith  ",
                        "correct-horse-battery",
                        Role.SELLER
                )
        );

        assertEquals(1L, response.id());
        assertEquals("sammy", response.username());
        assertEquals("Sam Smith", response.name());
        assertEquals(Role.SELLER, response.role());
        assertFalse(response.toString().contains("correct-horse-battery"));
        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertTrue(passwordEncoder.matches(
                "correct-horse-battery",
                savedUser.getValue().getPassword()
        ));
    }
}
