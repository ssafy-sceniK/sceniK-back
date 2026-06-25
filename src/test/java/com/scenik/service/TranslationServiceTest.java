package com.scenik.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scenik.domain.CommunityPost;
import com.scenik.domain.TranslationCache;
import com.scenik.domain.User;
import com.scenik.dto.TranslationDtos.TranslationRequest;
import com.scenik.dto.TranslationDtos.TranslationResponse;
import com.scenik.mapper.CommunityMapper;
import com.scenik.mapper.PlaceReviewMapper;
import com.scenik.mapper.TranslationCacheMapper;
import com.scenik.mapper.UserMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {
    private static final String GEMINI_PATH = "/generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent";

    @Mock TranslationCacheMapper translationCacheMapper;
    @Mock CommunityMapper communityMapper;
    @Mock PlaceReviewMapper placeReviewMapper;
    @Mock UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void geminiTranslationRequestUsesGenerateContentPayloadAndApiKeyHeader() throws Exception {
        AtomicReference<CapturedRequest> captured = new AtomicReference<>();
        HttpServer server = startGeminiStub(captured, """
                {"candidates":[{"content":{"parts":[{"text":"Translated result"}]}}]}
                """);

        try {
            TranslationService service = newService(server, "gemini-2.5-flash-lite", "");
            stubPost();

            TranslationResponse response = service.translate(user(), new TranslationRequest("POST", "post-1", "content", "en", null, null));

            assertThat(response.translatedText()).isEqualTo("Translated result");
            assertThat(captured.get().path()).isEqualTo(GEMINI_PATH);
            assertThat(captured.get().apiKey()).isEqualTo("test-gms-key");
            assertThat(captured.get().authorization()).isNull();

            JsonNode payload = objectMapper.readTree(captured.get().body());
            assertThat(payload.at("/contents/0/parts/0/text").asText())
                    .contains("You are a precise UI translation engine")
                    .contains("Target language: en")
                    .contains("Source language: ko")
                    .contains("서울 촬영지가 정말 좋아요.");

            ArgumentCaptor<TranslationCache> cacheCaptor = ArgumentCaptor.forClass(TranslationCache.class);
            verify(translationCacheMapper).insert(cacheCaptor.capture());
            assertThat(cacheCaptor.getValue().getModel()).isEqualTo("gemini-2.5-flash-lite");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void geminiTranslationResponseConcatenatesCandidatePartTexts() throws Exception {
        AtomicReference<CapturedRequest> captured = new AtomicReference<>();
        HttpServer server = startGeminiStub(captured, """
                {"candidates":[{"content":{"parts":[{"text":"Translated"},{"text":" result"}]}}]}
                """);

        try {
            TranslationService service = newService(server, "gemini-2.5-flash-lite", "");
            stubPost();

            TranslationResponse response = service.translate(user(), new TranslationRequest("POST", "post-1", "content", "en", null, null));

            assertThat(response.translatedText()).isEqualTo("Translated result");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void legacyOpenAiTranslationModelIsNormalizedToGeminiDefault() throws Exception {
        AtomicReference<CapturedRequest> captured = new AtomicReference<>();
        HttpServer server = startGeminiStub(captured, """
                {"candidates":[{"content":{"parts":[{"text":"Translated result"}]}}]}
                """);

        try {
            TranslationService service = newService(server, "gpt-5-nano", "gpt-5.4-mini");
            stubPost();

            TranslationResponse response = service.translate(user(), new TranslationRequest("POST", "post-1", "content", "en", null, null));

            assertThat(response.translatedText()).isEqualTo("Translated result");
            assertThat(captured.get().path()).isEqualTo(GEMINI_PATH);

            ArgumentCaptor<TranslationCache> cacheCaptor = ArgumentCaptor.forClass(TranslationCache.class);
            verify(translationCacheMapper).insert(cacheCaptor.capture());
            assertThat(cacheCaptor.getValue().getModel()).isEqualTo("gemini-2.5-flash-lite");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void primaryGeminiFailureFallsBackToConfiguredGeminiModel() throws Exception {
        List<CapturedRequest> captured = new ArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/generativelanguage.googleapis.com/v1beta/models/", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            captured.add(new CapturedRequest(
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestHeaders().getFirst("x-goog-api-key"),
                    exchange.getRequestHeaders().getFirst("Authorization"),
                    body
            ));
            if (exchange.getRequestURI().getPath().contains("gemini-2.5-flash-lite:generateContent")) {
                byte[] response = "{\"error\":{\"message\":\"model temporarily unavailable\"}}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(503, response.length);
                exchange.getResponseBody().write(response);
            } else {
                byte[] response = """
                        {"candidates":[{"content":{"parts":[{"text":"Fallback translated result"}]}}]}
                        """.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
            }
            exchange.close();
        });
        server.start();

        try {
            TranslationService service = newService(server, "gemini-2.5-flash-lite", "gemini-2.5-flash");
            stubPost();

            TranslationResponse response = service.translate(user(), new TranslationRequest("POST", "post-1", "content", "en", null, null));

            assertThat(response.translatedText()).isEqualTo("Fallback translated result");
            assertThat(captured).hasSize(2);
            assertThat(captured.get(0).path()).isEqualTo(GEMINI_PATH);
            assertThat(captured.get(1).path()).isEqualTo("/generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent");

            ArgumentCaptor<TranslationCache> cacheCaptor = ArgumentCaptor.forClass(TranslationCache.class);
            verify(translationCacheMapper).insert(cacheCaptor.capture());
            assertThat(cacheCaptor.getValue().getModel()).isEqualTo("gemini-2.5-flash");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void systemTextTranslationUsesRequestSourceTextAndCacheKey() throws Exception {
        AtomicReference<CapturedRequest> captured = new AtomicReference<>();
        HttpServer server = startGeminiStub(captured, """
                {"candidates":[{"content":{"parts":[{"text":"Translated course title"}]}}]}
                """);

        try {
            TranslationService service = newService(server, "gemini-2.5-flash-lite", "");
            when(translationCacheMapper.findLatest(eq("SYSTEM_TEXT"), eq("home-course:csv-route-1"), eq("title"), eq("ko"), eq("en"), any()))
                    .thenReturn(null);

            TranslationResponse response = service.translate(user(), new TranslationRequest(
                    "SYSTEM_TEXT",
                    "home-course:csv-route-1",
                    "title",
                    "en",
                    "?? ?? ?? ?? 2? ??",
                    "ko"
            ));

            assertThat(response.resourceType()).isEqualTo("SYSTEM_TEXT");
            assertThat(response.translatedText()).isEqualTo("Translated course title");
            assertThat(captured.get().body()).contains("?? ?? ?? ?? 2? ??");

            ArgumentCaptor<TranslationCache> cacheCaptor = ArgumentCaptor.forClass(TranslationCache.class);
            verify(translationCacheMapper).insert(cacheCaptor.capture());
            assertThat(cacheCaptor.getValue().getResourceType()).isEqualTo("SYSTEM_TEXT");
            assertThat(cacheCaptor.getValue().getResourceId()).isEqualTo("home-course:csv-route-1");
            assertThat(cacheCaptor.getValue().getFieldName()).isEqualTo("title");
        } finally {
            server.stop(0);
        }
    }

    private TranslationService newService(HttpServer server, String model, String fallbackModel) {
        return new TranslationService(
                translationCacheMapper,
                communityMapper,
                placeReviewMapper,
                userMapper,
                new LanguageDetectionService(),
                "http://127.0.0.1:" + server.getAddress().getPort() + "/",
                "test-gms-key",
                model,
                fallbackModel,
                1000,
                1000
        );
    }

    private User user() {
        User user = new User();
        user.setUserId(1L);
        user.setLanguage("en");
        return user;
    }

    private void stubPost() {
        CommunityPost post = new CommunityPost();
        post.setPostId("post-1");
        post.setLanguage("ko");
        post.setContent("서울 촬영지가 정말 좋아요.");
        when(communityMapper.findPostById("post-1", 1L)).thenReturn(post);
        when(translationCacheMapper.findLatest(eq("POST"), eq("post-1"), eq("content"), eq("ko"), eq("en"), any()))
                .thenReturn(null);
    }

    private HttpServer startGeminiStub(AtomicReference<CapturedRequest> captured, String responseJson) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(GEMINI_PATH, exchange -> {
            captured.set(new CapturedRequest(
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestHeaders().getFirst("x-goog-api-key"),
                    exchange.getRequestHeaders().getFirst("Authorization"),
                    new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)
            ));
            byte[] response = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        return server;
    }

    private record CapturedRequest(String path, String apiKey, String authorization, String body) {
    }
}
