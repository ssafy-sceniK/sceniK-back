package com.scenik.service;

import com.scenik.domain.AuthToken;
import com.scenik.domain.User;
import com.scenik.dto.AuthDtos.AuthResponse;
import com.scenik.dto.AuthDtos.EmailAvailabilityResponse;
import com.scenik.dto.AuthDtos.LoginRequest;
import com.scenik.dto.AuthDtos.RefreshTokenRequest;
import com.scenik.dto.AuthDtos.SignupRequest;
import com.scenik.dto.AuthDtos.UpdateUserRequest;
import com.scenik.dto.UserDto;
import com.scenik.mapper.AuthTokenMapper;
import com.scenik.mapper.UserMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final UserMapper userMapper;
    private final AuthTokenMapper authTokenMapper;
    private final PasswordService passwordService;

    public AuthService(UserMapper userMapper, AuthTokenMapper authTokenMapper, PasswordService passwordService) {
        this.userMapper = userMapper;
        this.authTokenMapper = authTokenMapper;
        this.passwordService = passwordService;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.email());
        String nickname = requireText(request.nickname(), "Nickname is required.");
        if (userMapper.findByEmail(email) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        }
        if (userMapper.findByNickname(nickname) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nickname already exists.");
        }
        User user = new User();
        user.setEmail(email);
        user.setNickname(nickname);
        user.setPasswordHash(passwordService.hashPassword(request.password()));
        user.setPreferences(UserDto.join(request.preferences()));
        user.setLanguage(normalizeLanguage(request.language()));
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        }
        String token = issueToken(user.getUserId());
        return new AuthResponse(token, token, UserDto.from(userMapper.findById(user.getUserId())));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userMapper.findByEmail(normalizeEmail(request.email()));
        if (user == null || !passwordService.verifyPassword(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }
        String token = issueToken(user.getUserId());
        return new AuthResponse(token, token, UserDto.from(user));
    }

    public EmailAvailabilityResponse checkEmail(String email) {
        String normalized = normalizeEmail(email);
        boolean available = userMapper.findByEmail(normalized) == null;
        return new EmailAvailabilityResponse(
                normalized,
                available,
                available ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다."
        );
    }

    public User requireUser(String authorizationHeader) {
        AuthToken token = requireActiveToken(parseBearer(authorizationHeader));
        User user = userMapper.findById(token.getUserId());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User was not found.");
        }
        return user;
    }

    public Long optionalUserId(String authorizationHeader) {
        String rawToken = parseBearerIfPresent(authorizationHeader);
        if (rawToken == null) {
            return null;
        }
        return requireActiveToken(rawToken).getUserId();
    }

    @Transactional
    public void logout(String authorizationHeader) {
        String rawToken = parseBearer(authorizationHeader);
        authTokenMapper.revokeByHash(hashToken(rawToken));
    }

    @Transactional
    public AuthResponse refresh(String authorizationHeader, RefreshTokenRequest request) {
        String rawToken = firstText(request == null ? null : request.refreshToken(), parseBearerIfPresent(authorizationHeader));
        if (rawToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is required.");
        }
        AuthToken existing = requireActiveToken(rawToken);
        User user = userMapper.findById(existing.getUserId());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User was not found.");
        }
        authTokenMapper.revokeByHash(hashToken(rawToken));
        String newToken = issueToken(user.getUserId());
        return new AuthResponse(newToken, newToken, UserDto.from(user));
    }

    @Transactional
    public AuthResponse issueSession(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User was not found.");
        }
        String token = issueToken(userId);
        return new AuthResponse(token, token, UserDto.from(user));
    }

    @Transactional
    public UserDto updateMe(String authorizationHeader, UpdateUserRequest request) {
        User user = requireUser(authorizationHeader);
        String nickname = requireText(request.nickname(), "Nickname is required.");
        String language = normalizeLanguage(firstText(request.language(), user.getLanguage()));
        userMapper.updateProfile(user.getUserId(), nickname, trimOptional(request.bio()), UserDto.join(request.preferences()), language);
        return UserDto.from(userMapper.findById(user.getUserId()));
    }

    @Transactional
    public UserDto updateProfileImage(String authorizationHeader, String profileImageUrl) {
        User user = requireUser(authorizationHeader);
        String imageUrl = requireText(profileImageUrl, "Profile image URL is required.");
        userMapper.updateProfileImage(user.getUserId(), imageUrl);
        return UserDto.from(userMapper.findById(user.getUserId()));
    }
    @Transactional
    public UserDto deleteProfileImage(String authorizationHeader) {
        User user = requireUser(authorizationHeader);
        userMapper.clearProfileImage(user.getUserId());
        return UserDto.from(userMapper.findById(user.getUserId()));
    }

    private String issueToken(Long userId) {
        byte[] tokenBytes = new byte[32];
        RANDOM.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        AuthToken token = new AuthToken();
        token.setUserId(userId);
        token.setTokenHash(hashToken(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusDays(30));
        authTokenMapper.insert(token);
        return rawToken;
    }

    private String parseBearer(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bearer token is required.");
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bearer token is required.");
        }
        return token;
    }

    private String parseBearerIfPresent(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        return parseBearer(authorizationHeader);
    }

    private AuthToken requireActiveToken(String rawToken) {
        AuthToken token = authTokenMapper.findActiveByHash(hashToken(rawToken));
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }
        return token;
    }

    private String hashToken(String rawToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not hash token.", ex);
        }
    }

    private String normalizeEmail(String email) {
        String normalized = requireText(email, "Email is required.").toLowerCase();
        if (!normalized.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email must be valid.");
        }
        return normalized;
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) return "ko";
        String lower = language.trim().replace('_', '-').toLowerCase();
        if (lower.startsWith("ko")) return "ko";
        if (lower.startsWith("en")) return "en";
        if (lower.startsWith("ja")) return "ja";
        if (lower.equals("zh-cn") || lower.startsWith("zh-hans") || lower.startsWith("zh")) return "zh-CN";
        return "ko";
    }

    private String trimOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private String firstText(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return null;
    }
}
