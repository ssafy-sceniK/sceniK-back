package com.scenik.service;

import com.scenik.domain.OAuthAccount;
import com.scenik.domain.OAuthLoginCode;
import com.scenik.domain.User;
import com.scenik.dto.AuthDtos.AuthResponse;
import com.scenik.mapper.OAuthAccountMapper;
import com.scenik.mapper.OAuthLoginCodeMapper;
import com.scenik.mapper.UserMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OAuthService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int LOGIN_CODE_TTL_MINUTES = 5;

    private final UserMapper userMapper;
    private final OAuthAccountMapper oauthAccountMapper;
    private final OAuthLoginCodeMapper oauthLoginCodeMapper;
    private final AuthService authService;

    public OAuthService(
            UserMapper userMapper,
            OAuthAccountMapper oauthAccountMapper,
            OAuthLoginCodeMapper oauthLoginCodeMapper,
            AuthService authService
    ) {
        this.userMapper = userMapper;
        this.oauthAccountMapper = oauthAccountMapper;
        this.oauthLoginCodeMapper = oauthLoginCodeMapper;
        this.authService = authService;
    }

    @Transactional
    public String completeLogin(String provider, Map<String, Object> attributes) {
        String normalizedProvider = requireText(provider, "OAuth provider is required.").toLowerCase(Locale.ROOT);
        if (!"google".equals(normalizedProvider)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported OAuth provider.");
        }

        String providerUserId = requireText(attribute(attributes, "sub"), "OAuth provider user ID is required.");
        OAuthAccount existingAccount = oauthAccountMapper.findByProviderUserId(normalizedProvider, providerUserId);
        if (existingAccount != null) {
            return issueLoginCode(existingAccount.getUserId());
        }

        String email = normalizeEmail(attribute(attributes, "email"));
        if (!isTrue(attributes.get("email_verified"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OAuth email is not verified.");
        }

        User user = userMapper.findByEmail(email);
        if (user == null) {
            user = createOAuthUser(email, attribute(attributes, "name"));
        }

        OAuthAccount account = new OAuthAccount();
        account.setUserId(user.getUserId());
        account.setProvider(normalizedProvider);
        account.setProviderUserId(providerUserId);
        account.setProviderEmail(email);
        oauthAccountMapper.insert(account);
        return issueLoginCode(user.getUserId());
    }

    @Transactional
    public AuthResponse exchangeCode(String rawCode) {
        String code = requireText(rawCode, "OAuth login code is required.");
        OAuthLoginCode loginCode = oauthLoginCodeMapper.findActiveByHashForUpdate(hash(code));
        if (loginCode == null || oauthLoginCodeMapper.consume(loginCode.getOauthLoginCodeId()) != 1) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired OAuth login code.");
        }
        return authService.issueSession(loginCode.getUserId());
    }

    private User createOAuthUser(String email, String displayName) {
        User user = new User();
        user.setEmail(email);
        user.setNickname(uniqueNickname(displayName, email));
        user.setPasswordHash("oauth_only$" + UUID.randomUUID());
        userMapper.insert(user);
        return user;
    }

    private String uniqueNickname(String displayName, String email) {
        String fallback = email.substring(0, email.indexOf('@'));
        String base = requireTextOrDefault(displayName, fallback)
                .replaceAll("[\\p{Cntrl}]", "")
                .trim();
        if (base.isBlank()) base = "sceniK user";
        if (base.length() > 45) base = base.substring(0, 45);

        String candidate = base;
        int suffix = 2;
        while (userMapper.findByNickname(candidate) != null) {
            candidate = base + " " + suffix++;
        }
        return candidate;
    }

    private String issueLoginCode(Long userId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String rawCode = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        OAuthLoginCode code = new OAuthLoginCode();
        code.setUserId(userId);
        code.setCodeHash(hash(rawCode));
        code.setExpiresAt(LocalDateTime.now().plusMinutes(LOGIN_CODE_TTL_MINUTES));
        oauthLoginCodeMapper.insert(code);
        return rawCode;
    }

    private String normalizeEmail(String email) {
        String normalized = requireText(email, "OAuth email is required.").toLowerCase(Locale.ROOT);
        if (!normalized.contains("@")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OAuth email is invalid.");
        }
        return normalized;
    }

    private String attribute(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private boolean isTrue(Object value) {
        return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
        }
        return value.trim();
    }

    private String requireTextOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not hash OAuth login code.", ex);
        }
    }
}
