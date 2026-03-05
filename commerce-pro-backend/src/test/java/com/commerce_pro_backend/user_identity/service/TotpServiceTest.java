package com.commerce_pro_backend.user_identity.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.Test;

class TotpServiceTest {

    @Test
    void shouldGenerateSecretAndOtpAuthUrl() {
        TotpService totpService = new TotpService("Commerce Pro Test");

        String secret = totpService.generateSecret();
        String url = totpService.buildOtpAuthUrl("alice", secret);

        assertNotNull(secret);
        assertTrue(secret.length() >= 16);
        assertTrue(url.startsWith("otpauth://totp/"));
        assertTrue(url.contains("issuer=Commerce+Pro+Test"));
    }

    @Test
    void shouldVerifyValidCurrentCode() throws Exception {
        TotpService totpService = new TotpService("Commerce Pro Test");
        String secret = totpService.generateSecret();

        String currentCode = generateCodeForNow(secret);

        assertTrue(totpService.verifyCode(secret, currentCode));
    }

    @Test
    void shouldRejectInvalidCode() {
        TotpService totpService = new TotpService("Commerce Pro Test");
        String secret = totpService.generateSecret();

        assertFalse(totpService.verifyCode(secret, "000000"));
        assertFalse(totpService.verifyCode(secret, "abc123"));
    }

    private String generateCodeForNow(String base32Secret) throws Exception {
        byte[] key = new Base32().decode(base32Secret);
        long timeStep = Instant.now().getEpochSecond() / 30;
        byte[] data = ByteBuffer.allocate(8).putLong(timeStep).array();

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);

        int otp = binary % 1_000_000;
        return String.format("%06d", otp);
    }
}
