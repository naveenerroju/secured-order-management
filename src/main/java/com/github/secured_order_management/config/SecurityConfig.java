package com.github.secured_order_management.config;

import com.github.secured_order_management.entity.Role;
import com.github.secured_order_management.entity.User;
import com.github.secured_order_management.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User user = userRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if (user.getRole() == null || user.getPassword() == null) {
                throw new UsernameNotFoundException("User credentials are incomplete");
            }

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/users/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products", "/products/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/products", "/products/**")
                        .hasAnyRole(Role.SELLER.name(), Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/products", "/products/**")
                        .hasAnyRole(Role.SELLER.name(), Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PATCH, "/products", "/products/**")
                        .hasAnyRole(Role.SELLER.name(), Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/products", "/products/**")
                        .hasAnyRole(Role.SELLER.name(), Role.ADMIN.name())
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .anyRequest().denyAll()
                )
                .build();
    }
}
