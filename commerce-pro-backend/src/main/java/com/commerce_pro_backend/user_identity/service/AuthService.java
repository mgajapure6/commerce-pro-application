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
import com.commerce_pro_backend.user_identity.dto.MfaDisableRequest;
import com.commerce_pro_backend.user_identity.dto.MfaSetupResponse;
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
    private final TotpService totpService;
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
        return user.getMfaSecret() != null && totpService.verifyCode(user.getMfaSecret(), code);
    }

    @Transactional
    public MfaSetupResponse setupMfa(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> ApiException.notFound("User", userId));

        String secret = totpService.generateSecret();
        user.setMfaSecret(secret);
        user.setMfaEnabled(false);
        userRepository.save(user);

        String otpAuthUrl = totpService.buildOtpAuthUrl(user.getUsername(), secret);

        auditService.log(userId, AuditAction.MFA_VERIFIED, "USER", userId,
            user.getUsername(), null, "MFA setup initiated", true);

        return MfaSetupResponse.builder()
            .secret(secret)
            .otpauthUrl(otpAuthUrl)
            .issuer(totpService.getIssuer())
            .accountName(user.getUsername())
            .build();
    }

    @Transactional
    public void enableMfa(String userId, String code) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> ApiException.notFound("User", userId));

        if (user.getMfaSecret() == null) {
            throw ApiException.badRequest("MFA setup has not been initialized");
        }

        if (!totpService.verifyCode(user.getMfaSecret(), code)) {
            throw ApiException.badRequest("Invalid MFA code");
        }

        user.setMfaEnabled(true);
        userRepository.save(user);

        auditService.log(userId, AuditAction.MFA_ENABLED, "USER", userId,
            user.getUsername(), null, "MFA enabled", true);
    }

    @Transactional
    public void disableMfa(String userId, MfaDisableRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> ApiException.notFound("User", userId));

        if (!Boolean.TRUE.equals(user.getMfaEnabled())) {
            throw ApiException.badRequest("MFA is not enabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("Password is incorrect");
        }

        if (!totpService.verifyCode(user.getMfaSecret(), request.getCode())) {
            throw ApiException.badRequest("Invalid MFA code");
        }

        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);

        auditService.log(userId, AuditAction.MFA_DISABLED, "USER", userId,
            user.getUsername(), null, "MFA disabled", true);
    }
}
