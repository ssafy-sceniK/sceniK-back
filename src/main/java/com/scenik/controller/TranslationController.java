package com.scenik.controller;

import com.scenik.domain.User;
import com.scenik.dto.ApiResponse;
import com.scenik.dto.TranslationDtos.TranslationRequest;
import com.scenik.dto.TranslationDtos.TranslationResponse;
import com.scenik.service.AuthService;
import com.scenik.service.TranslationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TranslationController {
    private final AuthService authService;
    private final TranslationService translationService;

    public TranslationController(AuthService authService, TranslationService translationService) {
        this.authService = authService;
        this.translationService = translationService;
    }

    @PostMapping("/translations")
    public ApiResponse<TranslationResponse> translate(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody TranslationRequest request
    ) {
        User user = authService.requireUser(authorization);
        return ApiResponse.ok(translationService.translate(user, request));
    }
}