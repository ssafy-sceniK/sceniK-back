package com.scenik.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record SignupRequest(
            @NotBlank(message = "이메일을 입력해 주세요.")
            @Email(message = "올바른 이메일 형식이 아닙니다.")
            String email,
            @NotBlank(message = "비밀번호를 입력해 주세요.")
            @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
            String password,
            @NotBlank(message = "닉네임을 입력해 주세요.")
            @Size(max = 50, message = "닉네임은 50자 이하로 입력해 주세요.")
            String nickname,
            List<String> preferences
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "이메일을 입력해 주세요.")
            @Email(message = "올바른 이메일 형식이 아닙니다.")
            String email,
            @NotBlank(message = "비밀번호를 입력해 주세요.")
            String password
    ) {
    }

    public record RefreshTokenRequest(@NotBlank(message = "로그인이 필요합니다.") String refreshToken) {
    }

    public record UpdateUserRequest(
            @NotBlank(message = "닉네임을 입력해 주세요.")
            @Size(max = 50, message = "닉네임은 50자 이하로 입력해 주세요.")
            String nickname,
            List<String> preferences
    ) {
    }

    public record AuthResponse(String accessToken, String refreshToken, UserDto user) {
    }
}
