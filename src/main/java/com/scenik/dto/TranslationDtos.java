package com.scenik.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public final class TranslationDtos {
    private TranslationDtos() {
    }

    public record TranslationRequest(
            @NotBlank(message = "resourceType is required.") String resourceType,
            @NotBlank(message = "resourceId is required.") String resourceId,
            @NotBlank(message = "field is required.") String field,
            String targetLanguage,
            String sourceText,
            String sourceLanguage
    ) {
    }

    public record TranslationResponse(
            String resourceType,
            String resourceId,
            String field,
            String sourceLanguage,
            String targetLanguage,
            String sourceText,
            String translatedText,
            boolean cached,
            LocalDateTime updatedAt
    ) {
    }
}