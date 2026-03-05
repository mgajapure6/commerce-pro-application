package com.commerce_pro_backend.user_identity.service;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.commerce_pro_backend.common.exception.ApiException;

@Service
public class TotpService {

    private static final int SECRET_BYTES = 20;
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final int VALIDATION_WINDOW_STEPS = 1;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base32 base32 = new Base32();
    private final String issuer;

    public TotpService(@Value("${app.mfa.issuer:Commerce Pro}") String issuer) {
        this.issuer = issuer;
    }

    public String generateSecret() {
        byte[] buffer = new byte[SECRET_BYTES];
        secureRandom.nextBytes(buffer);
        return base32.encodeToString(buffer).replace("=", "");
    }

    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null || !code.matches("^[0-9]{6}$")) {
            return false;
        }

        long currentStep = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        for (int i = -VALIDATION_WINDOW_STEPS; i <= VALIDATION_WINDOW_STEPS; i++) {
            String generatedCode = generateCodeForStep(secret, currentStep + i);
            if (code.equals(generatedCode)) {
                return true;
            }
        }
        return false;
    }

    public String buildOtpAuthUrl(String username, String secret) {
        String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        String encodedAccount = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String label = encodedIssuer + ":" + encodedAccount;

        return "otpauth://totp/" + label
                + "?secret=" + secret
                + "&issuer=" + encodedIssuer
                + "&algorithm=SHA1&digits=6&period=30";
    }

    public String getIssuer() {
        return issuer;
    }

    private String generateCodeForStep(String base32Secret, long timeStep) {
        try {
            byte[] key = base32.decode(base32Secret);
            byte[] timeBytes = ByteBuffer.allocate(8).putLong(timeStep).array();

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(timeBytes);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%06d", otp);
        } catch (GeneralSecurityException ex) {
            throw ApiException.badRequest("Unable to generate MFA code");
        }
    }
}
