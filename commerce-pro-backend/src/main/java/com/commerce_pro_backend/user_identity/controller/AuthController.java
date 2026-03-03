package com.commerce_pro_backend.user_identity.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.commerce_pro_backend.common.dto.ApiResponse;
import com.commerce_pro_backend.user_identity.dto.AuthRequest;
import com.commerce_pro_backend.user_identity.dto.AuthResponse;
import com.commerce_pro_backend.user_identity.dto.ChangePasswordRequest;
import com.commerce_pro_backend.user_identity.dto.RefreshTokenRequest;
import com.commerce_pro_backend.user_identity.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, logout, and token management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.authenticate(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(request.getRefreshToken())));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and invalidate tokens")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for authenticated user")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }
}