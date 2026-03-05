package com.commerce_pro_backend.user_identity.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commerce_pro_backend.common.exception.ApiException;
import com.commerce_pro_backend.user_identity.dto.AuthRequest;
import com.commerce_pro_backend.user_identity.dto.AuthResponse;
import com.commerce_pro_backend.user_identity.dto.ChangePasswordRequest;
import com.commerce_pro_backend.user_identity.entity.User;
import com.commerce_pro_backend.user_identity.enums.AuditAction;
import com.commerce_pro_backend.user_identity.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final AuditService auditService;
    private final SuperAdminSetupService superAdminSetupService;

    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("User not found"));
            
            // Check MFA if enabled
            if (user.getMfaEnabled()) {
                if (request.getMfaCode() == null || !verifyMfaCode(user, request.getMfaCode())) {
                    throw new BadCredentialsException("Invalid MFA code");
                }
            }
            
            // Update login info
            user.setLastLoginAt(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            
            boolean isSuperAdmin = superAdminSetupService.isSuperAdmin(user.getUsername());
            
            String accessToken = jwtTokenProvider.generateToken(authentication, isSuperAdmin);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
            
            auditService.log(user.getId(), AuditAction.LOGIN_SUCCESS, "USER", user.getId(), 
                user.getUsername(), null, "Login successful", true);
            
            return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L) // 15 minutes in seconds
                .issuedAt(LocalDateTime.now())
                .userId(user.getId())
                .username(user.getUsername())
                .superAdmin(isSuperAdmin)
                .build();
                
        } catch (BadCredentialsException e) {
            // Increment failed attempts
            userRepository.findByUsername(request.getUsername()).ifPresent(user -> {
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
                if (user.getFailedLoginAttempts() >= 5) {
                    user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
                }
                userRepository.save(user);
                
                auditService.log(user.getId(), AuditAction.LOGIN_FAILURE, "USER", user.getId(), 
                    user.getUsername(), null, "Failed login: " + e.getMessage(), false);
            });
            
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BadCredentialsException("User not found"));
        
        if (!user.getIsActive()) {
            throw new BadCredentialsException("User is deactivated");
        }
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Create authentication with effective authorities for refreshed access token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        
        boolean isSuperAdmin = superAdminSetupService.isSuperAdmin(username);
        
        String newAccessToken = jwtTokenProvider.generateToken(authentication, isSuperAdmin);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);
        
        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(900L)
            .issuedAt(LocalDateTime.now())
            .userId(user.getId())
            .username(user.getUsername())
            .superAdmin(isSuperAdmin)
            .build();
    }

    @Transactional
    public void logout(String token) {
        // In stateless JWT, we can't truly invalidate the token
        // But we can add it to a blacklist or reduce its expiry
        // For now, just log the logout
        String username = jwtTokenProvider.getUsernameFromToken(token);
        
        userRepository.findByUsername(username).ifPresent(user -> {
            auditService.log(user.getId(), AuditAction.LOGOUT, "USER", user.getId(), 
                username, null, "User logged out", true);
        });
        
        SecurityContextHolder.clearContext();
    }

    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> ApiException.notFound("User", userId));
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        userRepository.save(user);
        
        auditService.log(userId, AuditAction.PASSWORD_CHANGED, "USER", userId, 
            user.getUsername(), null, "Password changed by user", true);
    }

    private boolean verifyMfaCode(User user, String code) {
        // Implement TOTP verification logic here
        // For now, placeholder
        return true;
    }
}
