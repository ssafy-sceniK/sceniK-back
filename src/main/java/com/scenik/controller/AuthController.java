package com.scenik.controller;

import com.scenik.dto.ApiResponse;
import com.scenik.dto.AuthDtos.AuthResponse;
import com.scenik.dto.AuthDtos.EmailAvailabilityResponse;
import com.scenik.dto.AuthDtos.LoginRequest;
import com.scenik.dto.AuthDtos.OAuthCodeExchangeRequest;
import com.scenik.dto.AuthDtos.RefreshTokenRequest;
import com.scenik.dto.AuthDtos.SignupRequest;
import com.scenik.dto.AuthDtos.UpdateUserRequest;
import com.scenik.dto.UserDto;
import com.scenik.service.AuthService;
import com.scenik.service.OAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class AuthController {
    private final AuthService authService;
    private final OAuthService oauthService;

    public AuthController(AuthService authService, OAuthService oauthService) {
        this.authService = authService;
        this.oauthService = oauthService;
    }

    @PostMapping("/api/auth/signup")
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok("회원가입이 완료되었습니다.", authService.signup(request));
    }

    @PostMapping("/api/auth/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("로그인되었습니다.", authService.login(request));
    }

    @GetMapping("/api/auth/oauth/google")
    public RedirectView startGoogleOAuth() {
        return new RedirectView("/api/auth/oauth/authorization/google");
    }

    @GetMapping("/api/auth/oauth/kakao")
    public RedirectView startKakaoOAuth() {
        return new RedirectView("/api/auth/oauth/authorization/kakao");
    }

    @PostMapping("/api/auth/oauth/exchange")
    public ApiResponse<AuthResponse> exchangeOAuthCode(@Valid @RequestBody OAuthCodeExchangeRequest request) {
        return ApiResponse.ok("OAuth 로그인이 완료되었습니다.", oauthService.exchangeCode(request.code()));
    }

    @GetMapping("/api/auth/check-email")
    public ApiResponse<EmailAvailabilityResponse> checkEmail(@RequestParam("email") String email) {
        return ApiResponse.ok(authService.checkEmail(email));
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
        return ResponseEntity.ok(ApiResponse.okMessage("로그아웃되었습니다."));
    }

    @PostMapping("/api/auth/token/refresh")
    public ApiResponse<AuthResponse> refresh(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody(required = false) RefreshTokenRequest request
    ) {
        return ApiResponse.ok("토큰이 갱신되었습니다.", authService.refresh(authorization, request));
    }

    @GetMapping("/api/users/me")
    public ApiResponse<UserDto> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResponse.ok(UserDto.from(authService.requireUser(authorization)));
    }

    @PatchMapping("/api/users/me")
    public ApiResponse<UserDto> updateMe(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ApiResponse.ok("프로필이 수정되었습니다.", authService.updateMe(authorization, request));
    }
}
