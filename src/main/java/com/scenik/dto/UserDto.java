package com.scenik.dto;

import com.scenik.domain.User;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public record UserDto(
        Long userId,
        String email,
        String nickname,
        String role,
        String profileImageUrl,
        String bio,
        String language,
        String authProvider,
        String loginProvider,
        List<String> preferences,
        Boolean preferencesCompleted,
        LocalDateTime createdAt
) {
    public static UserDto from(User user) {
        List<String> preferences = split(user.getPreferences());
        String provider = user.getAuthProvider() == null || user.getAuthProvider().isBlank()
                ? "email"
                : user.getAuthProvider();
        return new UserDto(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole() == null || user.getRole().isBlank() ? "ROLE_USER" : user.getRole(),
                user.getProfileImageUrl(),
                user.getBio(),
                normalizeLanguage(user.getLanguage()),
                provider,
                provider,
                preferences,
                preferences.size() >= 3,
                user.getCreatedAt()
        );
    }

    public static String join(List<String> values) {
        if (values == null || values.isEmpty()) return null;
        return String.join(",", values.stream().map(String::trim).filter(item -> !item.isBlank()).toList());
    }

    private static List<String> split(String values) {
        if (values == null || values.isBlank()) return List.of();
        return Arrays.stream(values.split(",")).map(String::trim).filter(item -> !item.isBlank()).toList();
    }

    private static String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) return "ko";
        String lower = language.trim().replace('_', '-').toLowerCase();
        if (lower.startsWith("ko")) return "ko";
        if (lower.startsWith("en")) return "en";
        if (lower.startsWith("ja")) return "ja";
        if (lower.equals("zh-cn") || lower.startsWith("zh-hans") || lower.startsWith("zh")) return "zh-CN";
        return "ko";
    }
}