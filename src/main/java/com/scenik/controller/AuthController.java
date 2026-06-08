package com.scenik.controller;

import com.scenik.dto.AuthDtos.AuthResponse;
import com.scenik.dto.AuthDtos.LoginRequest;
import com.scenik.dto.AuthDtos.RefreshTokenRequest;
import com.scenik.dto.AuthDtos.SignupRequest;
import com.scenik.dto.AuthDtos.UpdateUserRequest;
import com.scenik.dto.UserDto;
import com.scenik.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/signup")
    public AuthResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/api/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/auth/token/refresh")
    public AuthResponse refresh(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody(required = false) RefreshTokenRequest request
    ) {
        return authService.refresh(authorization, request);
    }

    @GetMapping("/api/users/me")
    public UserDto me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return UserDto.from(authService.requireUser(authorization));
    }

    @PatchMapping("/api/users/me")
    public UserDto updateMe(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return authService.updateMe(authorization, request);
    }
}
