package com.cultswap.backend.controller;

import com.cultswap.backend.dto.LoginRequest;
import com.cultswap.backend.dto.LoginResponse;
import com.cultswap.backend.dto.RegisterRequest;
import com.cultswap.backend.dto.RegisterResponse;
import com.cultswap.backend.entity.Role;
import com.cultswap.backend.entity.User;
import com.cultswap.backend.repository.RoleRepository;
import com.cultswap.backend.security.JwtService;
import com.cultswap.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService,JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        return new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        User user = userService.login(
                request.getEmail(),
                request.getPassword()
        );

        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        String token = jwtService.generateToken(user.getId(), roles);

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                token
        );
    }
}