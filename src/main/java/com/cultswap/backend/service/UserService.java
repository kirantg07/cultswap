package com.cultswap.backend.service;

import com.cultswap.backend.entity.Role;
import com.cultswap.backend.entity.User;
import com.cultswap.backend.exception.DuplicateEmailException;
import com.cultswap.backend.exception.InvalidCredentialsException;
import com.cultswap.backend.repository.RoleRepository;
import com.cultswap.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(String name, String email, String password) {

        // 1. Normalize email
        email = email.trim().toLowerCase();

        // 2. Check whether email already exists
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException("Email is already registered");
        }

        // 3. Hash password
        String hashedPassword = passwordEncoder.encode(password);

        // 4. Find BUYER role
        Role buyerRole = roleRepository.findByName("BUYER")
                .orElseThrow(() -> new IllegalStateException("BUYER role not found"));

        // 5. Create user
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);
        user.setEnabled(true);

        // 6. Assign role
        user.setRoles(new java.util.HashSet<>(Set.of(buyerRole)));

        // 7. Save user
        return userRepository.save(user);
    }

    public User login(String email, String password) {

        email = email.trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

        boolean passwordMatches = passwordEncoder.matches(
                password,
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return user;
    }
}