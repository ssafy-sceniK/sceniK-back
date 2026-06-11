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
        List<String> preferences,
        LocalDateTime createdAt
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole() == null || user.getRole().isBlank() ? "ROLE_USER" : user.getRole(),
                split(user.getPreferences()),
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
}
