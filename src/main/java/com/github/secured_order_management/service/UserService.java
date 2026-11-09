package com.github.secured_order_management.service;

import com.github.secured_order_management.dto.CreateUserRequest;
import com.github.secured_order_management.dto.UserResponse;
import com.github.secured_order_management.entity.User;
import com.github.secured_order_management.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse registerUser(CreateUserRequest request) {
        User user = new User();
        user.setName(request.name().trim());
        user.setRole(request.role());
        user.setPassword(passwordEncoder.encode(request.password()));

        return UserResponse.from(userRepository.save(user));
    }
}
