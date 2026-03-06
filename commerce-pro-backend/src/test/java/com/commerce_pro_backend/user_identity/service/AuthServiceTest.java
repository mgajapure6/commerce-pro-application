package com.commerce_pro_backend.user_identity.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.commerce_pro_backend.user_identity.dto.AuthRequest;
import com.commerce_pro_backend.user_identity.dto.AuthResponse;
import com.commerce_pro_backend.user_identity.entity.RefreshToken;
import com.commerce_pro_backend.user_identity.entity.User;
import com.commerce_pro_backend.user_identity.enums.AuditAction;
import com.commerce_pro_backend.user_identity.repository.RefreshTokenRepository;
import com.commerce_pro_backend.user_identity.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock CustomUserDetailsService userDetailsService;
    @Mock TotpService totpService;
    @Mock AuditService auditService;
    @Mock SuperAdminSetupService superAdminSetupService;
    @Mock EmailNotificationService emailNotificationService;

    @InjectMocks AuthService authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
            .id("user-1")
            .username("alice")
            .email("alice@example.com")
            .passwordHash("$encoded$")
            .isActive(true)
            .mfaEnabled(false)
            .failedLoginAttempts(0)
            .build();
    }

    // -------------------------------------------------------------------------
    // Issue 2: Account lock check
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("authenticate() should throw LockedException when account is locked")
    void authenticate_shouldThrowWhenLocked() {
        activeUser.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(activeUser));

        AuthRequest request = AuthRequest.builder()
            .username("alice").password("pass").build();

        assertThatThrownBy(() -> authService.authenticate(request))
            .isInstanceOf(LockedException.class)
            .hasMessageContaining("locked");

        // AuthenticationManager should NOT be called for locked accounts
        verifyNoInteractions(authenticationManager);
    }

    @Test
    @DisplayName("authenticate() should allow login after lock period expires")
    void authenticate_shouldAllowLoginAfterLockExpires() {
        activeUser.setLockedUntil(LocalDateTime.now().minusMinutes(1)); // expired lock
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(activeUser));
        Authentication auth = mock(Authentication.class);
        when(auth.getAuthorities()).thenAnswer(i -> List.of());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtTokenProvider.generateToken(any(), anyBoolean())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtTokenProvider.hashToken(any())).thenReturn("hashed");
        when(jwtTokenProvider.getRefreshExpirationMs()).thenReturn(604800000L);
        when(superAdminSetupService.isSuperAdmin(any())).thenReturn(false);

        AuthRequest request = AuthRequest.builder()
            .username("alice").password("pass").build();

        AuthResponse response = authService.authenticate(request);
        assertThat(response.getAccessToken()).isEqualTo("access-token");
    }

    // -------------------------------------------------------------------------
    // Issue 5: MFA timing — context not set before MFA verified
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("authenticate() should not set SecurityContext when MFA code is invalid")
    void authenticate_shouldNotSetContextOnInvalidMfa() {
        activeUser.setMfaEnabled(true);
        activeUser.setMfaSecret("secret");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(activeUser));
        Authentication auth = mock(Authentication.class);
        when(auth.getAuthorities()).thenAnswer(i -> List.of());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(totpService.verifyCode("secret", "000000")).thenReturn(false);

        AuthRequest request = AuthRequest.builder()
            .username("alice").password("pass").mfaCode("000000").build();

        assertThatThrownBy(() -> authService.authenticate(request))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("MFA");
    }

    // -------------------------------------------------------------------------
    // Issue 1: Refresh token revocation / reuse detection
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("refreshToken() should throw when token is not found in DB")
    void refreshToken_shouldThrowWhenNotInDb() {
        when(jwtTokenProvider.validateToken("raw-token")).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken("raw-token")).thenReturn(true);
        when(jwtTokenProvider.hashToken("raw-token")).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken("raw-token"))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("not recognised");
    }

    @Test
    @DisplayName("refreshToken() should revoke entire family on token reuse (theft detection)")
    void refreshToken_shouldRevokeFamilyOnReuse() {
        when(jwtTokenProvider.validateToken("raw-token")).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken("raw-token")).thenReturn(true);
        when(jwtTokenProvider.hashToken("raw-token")).thenReturn("hash");

        RefreshToken storedRevoked = RefreshToken.builder()
            .tokenHash("hash")
            .username("alice")
            .family("family-uuid")
            .revoked(true)  // Already revoked — indicates reuse!
            .expiresAt(LocalDateTime.now().plusDays(7))
            .build();
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(storedRevoked));

        assertThatThrownBy(() -> authService.refreshToken("raw-token"))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("already used");

        verify(refreshTokenRepository).revokeAllByFamily(eq("family-uuid"), any(), anyString());
    }

    @Test
    @DisplayName("logout() should revoke all user refresh tokens in DB")
    void logout_shouldRevokeAllUserTokens() {
        when(jwtTokenProvider.getUsernameFromToken("access-token")).thenReturn("alice");
        when(refreshTokenRepository.revokeAllByUsername(eq("alice"), any(), anyString())).thenReturn(2);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(activeUser));

        authService.logout("access-token");

        verify(refreshTokenRepository).revokeAllByUsername(eq("alice"), any(), anyString());
    }

    // -------------------------------------------------------------------------
    // Issue 2: Failed attempt counter and auto-lock
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("authenticate() should increment failed attempts on bad credentials")
    void authenticate_shouldIncrementFailedAttemptsOnBadCredentials() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(activeUser));
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad password"));

        AuthRequest request = AuthRequest.builder()
            .username("alice").password("wrong").build();

        assertThatThrownBy(() -> authService.authenticate(request))
            .isInstanceOf(BadCredentialsException.class);

        assertThat(activeUser.getFailedLoginAttempts()).isEqualTo(1);
        verify(userRepository, atLeastOnce()).save(activeUser);
    }

    @Test
    @DisplayName("authenticate() should lock account after 5 failed attempts")
    void authenticate_shouldLockAfterFiveFailures() {
        activeUser.setFailedLoginAttempts(4);  // One more push will lock
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(activeUser));
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad password"));

        AuthRequest request = AuthRequest.builder()
            .username("alice").password("wrong").build();

        assertThatThrownBy(() -> authService.authenticate(request))
            .isInstanceOf(BadCredentialsException.class);

        assertThat(activeUser.getLockedUntil()).isNotNull();
        assertThat(activeUser.getLockedUntil()).isAfter(LocalDateTime.now());
        verify(emailNotificationService).sendAccountLockedNotification(
            eq(activeUser.getEmail()), eq(activeUser.getUsername()), anyInt());
    }
}
