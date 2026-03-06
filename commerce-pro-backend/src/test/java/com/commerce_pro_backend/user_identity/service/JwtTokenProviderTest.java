package com.commerce_pro_backend.user_identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class JwtTokenProviderTest {

    private static final String VALID_SECRET =
        "test-secret-key-that-is-at-least-32-characters-long!";
    private static final long ACCESS_EXPIRY_MS  = 900_000L;   // 15 min
    private static final long REFRESH_EXPIRY_MS = 604_800_000L; // 7 days

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() throws Exception {
        provider = new JwtTokenProvider(VALID_SECRET, ACCESS_EXPIRY_MS, REFRESH_EXPIRY_MS);
        provider.init();
    }

    @Test
    @DisplayName("init() should reject default placeholder secret")
    void init_shouldRejectDefaultPlaceholder() {
        JwtTokenProvider bad = new JwtTokenProvider(
            "your-256-bit-secret-key-here-must-be-at-least-32-characters-long",
            ACCESS_EXPIRY_MS, REFRESH_EXPIRY_MS);
        assertThatThrownBy(bad::init)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("default placeholder");
    }

    @Test
    @DisplayName("init() should reject secrets shorter than 32 characters")
    void init_shouldRejectShortSecret() {
        JwtTokenProvider bad = new JwtTokenProvider("tooshort", ACCESS_EXPIRY_MS, REFRESH_EXPIRY_MS);
        assertThatThrownBy(bad::init)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("at least 32");
    }

    @Test
    @DisplayName("generateToken() should produce a valid token with correct subject")
    void generateToken_shouldProduceValidToken() {
        Authentication auth = mockAuthentication("alice",
            List.of(new SimpleGrantedAuthority("ROLE_USER")));
        String token = provider.generateToken(auth, false);

        assertThat(token).isNotBlank();
        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUsernameFromToken(token)).isEqualTo("alice");
    }

    @Test
    @DisplayName("generateToken() should embed isSuperAdmin claim")
    void generateToken_shouldEmbedSuperAdminClaim() {
        Authentication auth = mockAuthentication("admin", List.of());
        String token = provider.generateToken(auth, true);

        var claims = provider.parseToken(token);
        assertThat(claims.get("isSuperAdmin", Boolean.class)).isTrue();
    }

    @Test
    @DisplayName("generateRefreshToken() should be identifiable as a refresh token")
    void generateRefreshToken_shouldBeIdentifiableAsRefresh() {
        String refresh = provider.generateRefreshToken("alice");
        assertThat(provider.isRefreshToken(refresh)).isTrue();
        assertThat(provider.validateToken(refresh)).isTrue();
    }

    @Test
    @DisplayName("validateToken() should return false for a tampered token")
    void validateToken_shouldReturnFalseForTamperedToken() {
        Authentication auth = mockAuthentication("alice", List.of());
        String token = provider.generateToken(auth, false);
        String tampered = token.substring(0, token.length() - 4) + "xxxx";
        assertThat(provider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("validateToken() should return false for an expired token")
    void validateToken_shouldReturnFalseForExpiredToken() throws Exception {
        // Create a provider with 1ms expiry
        JwtTokenProvider shortLived = new JwtTokenProvider(VALID_SECRET, 1L, 1L);
        shortLived.init();
        Authentication auth = mockAuthentication("alice", List.of());
        String token = shortLived.generateToken(auth, false);
        Thread.sleep(10);
        assertThat(shortLived.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("hashToken() should produce consistent 64-char hex output")
    void hashToken_shouldProduceConsistentHash() {
        String raw = "some-refresh-token-value";
        String hash1 = provider.hashToken(raw);
        String hash2 = provider.hashToken(raw);
        assertThat(hash1).hasSize(64).isEqualTo(hash2);
        assertThat(provider.hashToken("different")).isNotEqualTo(hash1);
    }

    @Test
    @DisplayName("generateImpersonationToken() should be identifiable as impersonation")
    void generateImpersonationToken_shouldBeIdentifiable() {
        String token = provider.generateImpersonationToken("admin", "user-id-123", "targetUser");
        assertThat(provider.isImpersonationToken(token)).isTrue();
        assertThat(provider.getUsernameFromToken(token)).isEqualTo("targetUser");
    }

    private Authentication mockAuthentication(String username,
                                               List<? extends GrantedAuthority> authorities) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        when(auth.getAuthorities()).thenAnswer(inv -> authorities);
        return auth;
    }
}
