package com.scenik.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.scenik.domain.CommunityComment;
import com.scenik.domain.CommunityPost;
import com.scenik.domain.PlaceReview;
import com.scenik.domain.TranslationCache;
import com.scenik.domain.User;
import com.scenik.dto.TranslationDtos.TranslationRequest;
import com.scenik.dto.TranslationDtos.TranslationResponse;
import com.scenik.mapper.CommunityMapper;
import com.scenik.mapper.PlaceReviewMapper;
import com.scenik.mapper.TranslationCacheMapper;
import com.scenik.mapper.UserMapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TranslationService {
    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);
    private static final String DEFAULT_TRANSLATION_MODEL = "gpt-5-nano";
    private static final int MAX_TRANSLATION_SOURCE_LENGTH = 4000;

    private final TranslationCacheMapper translationCacheMapper;
    private final CommunityMapper communityMapper;
    private final PlaceReviewMapper placeReviewMapper;
    private final UserMapper userMapper;
    private final LanguageDetectionService languageDetectionService;
    private final RestClient restClient;
    private final String gmsBaseUrl;
    private final String gmsApiKey;
    private final String gmsModel;

    public TranslationService(
            TranslationCacheMapper translationCacheMapper,
            CommunityMapper communityMapper,
            PlaceReviewMapper placeReviewMapper,
            UserMapper userMapper,
            LanguageDetectionService languageDetectionService,
            @Value("${scenik.ai.gms.base-url}") String gmsBaseUrl,
            @Value("${scenik.ai.gms.api-key:}") String gmsApiKey,
            @Value("${scenik.ai.gms.translation-model:${scenik.ai.gms.model:gpt-5-nano}}") String gmsModel,
            @Value("${scenik.ai.gms.connect-timeout-ms:1000}") int connectTimeoutMs,
            @Value("${scenik.ai.gms.read-timeout-ms:5000}") int readTimeoutMs
    ) {
        this.translationCacheMapper = translationCacheMapper;
        this.communityMapper = communityMapper;
        this.placeReviewMapper = placeReviewMapper;
        this.userMapper = userMapper;
        this.languageDetectionService = languageDetectionService;
        this.gmsBaseUrl = normalizeBaseUrl(gmsBaseUrl);
        this.gmsApiKey = gmsApiKey == null ? "" : gmsApiKey.trim();
        this.gmsModel = normalizeGmsModel(gmsModel);
        if (gmsModel != null && !gmsModel.trim().equals(this.gmsModel)) {
            log.info("Normalized GMS translation model from '{}' to '{}'.", gmsModel, this.gmsModel);
        }
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    @Transactional
    public TranslationResponse translate(User user, TranslationRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Translation request is required.");
        }
        String resourceType = normalizeResourceType(request.resourceType());
        String resourceId = requireText(request.resourceId(), "resourceId is required.");
        String fieldName = normalizeField(request.field());
        String targetLanguage = languageDetectionService.normalize(firstText(request.targetLanguage(), user.getLanguage()));
        SourceText source = resolveSource(resourceType, resourceId, fieldName, user);
        if (source.text().length() > MAX_TRANSLATION_SOURCE_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Translation source is too long.");
        }

        String sourceLanguage = languageDetectionService.detect(source.text(), source.language());
        if (sourceLanguage.equals(targetLanguage)) {
            return new TranslationResponse(resourceType, source.resourceId(), fieldName, sourceLanguage, targetLanguage, source.text(), source.text(), false, null);
        }

        String sourceHash = sha256(source.text());
        TranslationCache cached = translationCacheMapper.findLatest(resourceType, source.resourceId(), fieldName, sourceLanguage, targetLanguage, sourceHash);
        if (cached != null) {
            return new TranslationResponse(resourceType, source.resourceId(), fieldName, sourceLanguage, targetLanguage, cached.getSourceText(), cached.getTranslatedText(), true, cached.getUpdatedAt());
        }

        String translatedText = requestGmsTranslation(source.text(), sourceLanguage, targetLanguage);
        TranslationCache cache = new TranslationCache();
        cache.setUserId(user.getUserId());
        cache.setResourceType(resourceType);
        cache.setResourceId(source.resourceId());
        cache.setFieldName(fieldName);
        cache.setSourceLanguage(sourceLanguage);
        cache.setTargetLanguage(targetLanguage);
        cache.setSourceHash(sourceHash);
        cache.setSourceText(source.text());
        cache.setTranslatedText(translatedText);
        cache.setProvider("gms");
        cache.setModel(gmsModel);
        translationCacheMapper.insert(cache);
        return new TranslationResponse(resourceType, source.resourceId(), fieldName, sourceLanguage, targetLanguage, source.text(), translatedText, false, LocalDateTime.now());
    }

    private SourceText resolveSource(String resourceType, String resourceId, String fieldName, User viewer) {
        return switch (resourceType) {
            case "POST" -> sourceFromPost(resourceId, fieldName, viewer.getUserId());
            case "COMMENT" -> sourceFromComment(resourceId, fieldName, viewer.getUserId());
            case "REVIEW" -> sourceFromReview(resourceId, fieldName);
            case "PROFILE", "USER" -> sourceFromUser(resourceId, fieldName);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported resourceType.");
        };
    }
    private SourceText sourceFromPost(String resourceId, String fieldName, Long viewerUserId) {
        CommunityPost post = communityMapper.findPostById(resourceId, viewerUserId);
        if (post == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post was not found.");
        if ("title".equals(fieldName)) {
            return new SourceText(post.getPostId(), requireText(post.getTitle(), "Post title is empty."), post.getLanguage());
        }
        requireContentField(fieldName, "POST");
        return new SourceText(post.getPostId(), requireText(post.getContent(), "Post content is empty."), post.getLanguage());
    }

    private SourceText sourceFromComment(String resourceId, String fieldName, Long viewerUserId) {
        requireContentField(fieldName, "COMMENT");
        CommunityComment comment = communityMapper.findCommentById(resourceId, viewerUserId);
        if (comment == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment was not found.");
        return new SourceText(comment.getCommentId(), requireText(comment.getContent(), "Comment content is empty."), comment.getLanguage());
    }

    private SourceText sourceFromReview(String resourceId, String fieldName) {
        requireContentField(fieldName, "REVIEW");
        PlaceReview review = placeReviewMapper.findById(parseLong(resourceId, "reviewId"));
        if (review == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review was not found.");
        return new SourceText(String.valueOf(review.getReviewId()), requireText(review.getContent(), "Review content is empty."), review.getLanguage());
    }

    private SourceText sourceFromUser(String resourceId, String fieldName) {
        if (!"bio".equals(fieldName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PROFILE supports only bio translation.");
        }
        User profile = userMapper.findById(parseLong(resourceId, "userId"));
        if (profile == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User was not found.");
        return new SourceText(String.valueOf(profile.getUserId()), requireText(profile.getBio(), "Profile bio is empty."), profile.getLanguage());
    }

    private String requestGmsTranslation(String sourceText, String sourceLanguage, String targetLanguage) {
        if (gmsApiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Translation provider is not configured.");
        }
        String prompt = "Translate this user-generated sceniK text. Preserve names, place names, URLs, hashtags, emoji, and line breaks. "
                + "Return only the translated text, with no explanation. Source language: " + sourceLanguage
                + ". Target language: " + targetLanguage + ".\n\n" + sourceText;
        try {
            GmsChatResponse response = restClient.post()
                    .uri(URI.create(gmsBaseUrl + "api.openai.com/v1/chat/completions"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + gmsApiKey)
                    .body(new GmsChatRequest(
                            gmsModel,
                            List.of(
                                    new GmsMessage("developer", "You are a precise UI translation engine. Return only translated text."),
                                    new GmsMessage("user", prompt)
                            )
                    ))
                    .retrieve()
                    .body(GmsChatResponse.class);
            String translated = response == null || response.choices() == null || response.choices().isEmpty()
                    ? null
                    : response.choices().get(0).content();
            if (translated == null || translated.isBlank()) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Translation provider returned an empty response.");
            }
            return translated.trim();
        } catch (RestClientResponseException exception) {
            log.warn(
                    "GMS translation request failed with status {} using model '{}': {}",
                    exception.getStatusCode().value(),
                    gmsModel,
                    truncateForLog(exception.getResponseBodyAsString())
            );
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Translation provider request failed.");
        } catch (RestClientException exception) {
            log.warn("GMS translation request failed using model '{}'.", gmsModel, exception);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Translation provider request failed.");
        }
    }
    private void requireContentField(String fieldName, String resourceType) {
        if (!"content".equals(fieldName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, resourceType + " supports only content translation.");
        }
    }

    private String normalizeResourceType(String resourceType) {
        String normalized = requireText(resourceType, "resourceType is required.").trim().toUpperCase();
        if ("POST".equals(normalized) || "COMMENT".equals(normalized) || "REVIEW".equals(normalized) || "PROFILE".equals(normalized) || "USER".equals(normalized)) {
            return normalized;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported resourceType.");
    }

    private String normalizeField(String field) {
        String normalized = requireText(field, "field is required.").trim().toLowerCase();
        return switch (normalized) {
            case "body", "text", "content" -> "content";
            case "title" -> "title";
            case "bio" -> "bio";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported translation field.");
        };
    }

    private Long parseLong(String value, String label) {
        try {
            return Long.parseLong(requireText(value, label + " is required."));
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " must be numeric.");
        }
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private String firstText(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) return preferred.trim();
        if (fallback != null && !fallback.isBlank()) return fallback.trim();
        return "ko";
    }

    private String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not hash translation source.", exception);
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        String value = baseUrl == null || baseUrl.isBlank() ? "https://gms.ssafy.io/gmsapi/" : baseUrl.trim();
        return value.endsWith("/") ? value : value + "/";
    }

    private String normalizeGmsModel(String model) {
        if (model == null || model.isBlank()) {
            return DEFAULT_TRANSLATION_MODEL;
        }
        String value = model.trim();
        String canonical = value.toLowerCase().replace('_', '-').replace(' ', '-');
        if ("gpt-nano".equals(canonical) || "gpt5-nano".equals(canonical) || DEFAULT_TRANSLATION_MODEL.equals(canonical)) {
            return DEFAULT_TRANSLATION_MODEL;
        }
        return value;
    }

    private String truncateForLog(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String sanitized = value.replaceAll("[\\r\\n]+", " ").trim();
        return sanitized.length() <= 500 ? sanitized : sanitized.substring(0, 500) + "...";
    }

    private record SourceText(String resourceId, String text, String language) {
    }

    private record GmsChatRequest(String model, List<GmsMessage> messages) {
    }

    private record GmsMessage(String role, String content) {
    }

    private record GmsChatResponse(List<GmsChoice> choices) {
    }

    private record GmsChoice(GmsResponseMessage message) {
        String content() {
            if (message == null || message.content() == null || message.content().isNull()) {
                return null;
            }
            JsonNode content = message.content();
            if (content.isTextual()) {
                return content.asText();
            }
            if (content.isArray()) {
                StringBuilder text = new StringBuilder();
                for (JsonNode part : content) {
                    if (part.isTextual()) {
                        text.append(part.asText());
                    } else if (part.hasNonNull("text")) {
                        text.append(part.get("text").asText());
                    } else if (part.hasNonNull("content")) {
                        text.append(part.get("content").asText());
                    }
                }
                return text.isEmpty() ? null : text.toString();
            }
            return null;
        }
    }

    private record GmsResponseMessage(String role, JsonNode content) {
    }
}
