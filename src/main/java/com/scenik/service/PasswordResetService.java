package com.scenik.service;

import com.scenik.domain.PasswordResetToken;
import com.scenik.domain.User;
import com.scenik.mapper.AuthTokenMapper;
import com.scenik.mapper.PasswordResetTokenMapper;
import com.scenik.mapper.UserMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PasswordResetService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RESET_TOKEN_TTL_MINUTES = 30;

    private final UserMapper userMapper;
    private final PasswordResetTokenMapper passwordResetTokenMapper;
    private final AuthTokenMapper authTokenMapper;
    private final PasswordService passwordService;
    private final PasswordResetMailSender passwordResetMailSender;
    private final String frontendResetUrl;

    public PasswordResetService(
            UserMapper userMapper,
            PasswordResetTokenMapper passwordResetTokenMapper,
            AuthTokenMapper authTokenMapper,
            PasswordService passwordService,
            PasswordResetMailSender passwordResetMailSender,
            @Value("${scenik.password-reset.frontend-url}") String frontendResetUrl
    ) {
        this.userMapper = userMapper;
        this.passwordResetTokenMapper = passwordResetTokenMapper;
        this.authTokenMapper = authTokenMapper;
        this.passwordService = passwordService;
        this.passwordResetMailSender = passwordResetMailSender;
        this.frontendResetUrl = frontendResetUrl;
    }

    @Transactional
    public void requestReset(String email) {
        String normalizedEmail = normalizeEmail(email);
        User user = userMapper.findByEmail(normalizedEmail);
        if (user == null || isDeleted(user) || isOAuthOnly(user)) {
            return;
        }

        String rawToken = issueRawToken();
        passwordResetTokenMapper.invalidateActiveByUserId(user.getUserId());
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(user.getUserId());
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_TTL_MINUTES));
        passwordResetTokenMapper.insert(token);
        passwordResetMailSender.sendResetLink(normalizedEmail, buildResetUrl(rawToken));
    }

    @Transactional
    public void confirmReset(String rawToken, String password) {
        String tokenValue = requireText(rawToken, "Password reset token is required.");
        PasswordResetToken token = passwordResetTokenMapper.findActiveByHashForUpdate(hash(tokenValue));
        if (token == null || passwordResetTokenMapper.consume(token.getPasswordResetTokenId()) != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset link is invalid or expired.");
        }
        User user = userMapper.findById(token.getUserId());
        if (user == null || isDeleted(user) || isOAuthOnly(user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset link is invalid or expired.");
        }
        userMapper.updatePassword(user.getUserId(), passwordService.hashPassword(password));
        passwordResetTokenMapper.invalidateActiveByUserId(user.getUserId());
        authTokenMapper.revokeByUserId(user.getUserId());
    }

    private String buildResetUrl(String rawToken) {
        String separator = frontendResetUrl.contains("?") ? "&" : "?";
        return frontendResetUrl + separator + "token=" + rawToken;
    }

    private String issueRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not hash password reset token.", exception);
        }
    }

    private String normalizeEmail(String email) {
        String normalized = requireText(email, "Email is required.").toLowerCase();
        if (!normalized.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email must be valid.");
        }
        return normalized;
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private boolean isDeleted(User user) {
        return "DELETED".equalsIgnoreCase(user.getStatus());
    }

    private boolean isOAuthOnly(User user) {
        return user.getPasswordHash() != null && user.getPasswordHash().startsWith("oauth_only$");
    }
}
