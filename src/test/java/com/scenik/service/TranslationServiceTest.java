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
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {
    @Mock TranslationCacheMapper translationCacheMapper;
    @Mock CommunityMapper communityMapper;
    @Mock PlaceReviewMapper placeReviewMapper;
    @Mock UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void gmsTranslationRequestUsesGpt5NanoModel() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>();
        HttpServer server = startGmsStub(
                requestBody,
                authorization,
                """
                        {"choices":[{"message":{"role":"assistant","content":"Translated result"}}]}
                        """
        );

        try {
            TranslationService service = new TranslationService(
                    translationCacheMapper,
                    communityMapper,
                    placeReviewMapper,
                    userMapper,
                    new LanguageDetectionService(),
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/",
                    "test-gms-key",
                    "gpt-5-nano",
                    "gpt-5.4-mini",
                    1000,
                    1000
            );

            User user = new User();
            user.setUserId(1L);
            user.setLanguage("en");
            CommunityPost post = new CommunityPost();
            post.setPostId("post-1");
            post.setLanguage("ko");
            post.setContent("서울 촬영지가 정말 좋아요.");

            when(communityMapper.findPostById("post-1", 1L)).thenReturn(post);
            when(translationCacheMapper.findLatest(eq("POST"), eq("post-1"), eq("content"), eq("ko"), eq("en"), any()))
                    .thenReturn(null);

            TranslationResponse response = service.translate(user, new TranslationRequest("POST", "post-1", "content", "en"));

            assertThat(response.translatedText()).isEqualTo("Translated result");
            assertThat(authorization.get()).isEqualTo("Bearer test-gms-key");

            JsonNode payload = objectMapper.readTree(requestBody.get());
            assertThat(payload.get("model").asText()).isEqualTo("gpt-5-nano");
            assertThat(payload.at("/messages/0/role").asText()).isEqualTo("system");
            assertThat(payload.at("/messages/1/role").asText()).isEqualTo("user");
            assertThat(payload.at("/messages/1/content").asText()).contains("Target language: en");

            ArgumentCaptor<TranslationCache> cacheCaptor = ArgumentCaptor.forClass(TranslationCache.class);
            verify(translationCacheMapper).insert(cacheCaptor.capture());
            assertThat(cacheCaptor.getValue().getModel()).isEqualTo("gpt-5-nano");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void gptNanoAliasIsNormalizedToGpt5Nano() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>();
        HttpServer server = startGmsStub(
                requestBody,
                authorization,
                """
                        {"choices":[{"message":{"role":"assistant","content":"Translated result"}}]}
                        """
        );

        try {
            TranslationService service = new TranslationService(
                    translationCacheMapper,
                    communityMapper,
                    placeReviewMapper,
                    userMapper,
                    new LanguageDetectionService(),
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/",
                    "test-gms-key",
                    "gpt-nano",
                    "gpt-5.4-mini",
                    1000,
                    1000
            );

            User user = new User();
            user.setUserId(1L);
            user.setLanguage("en");
            CommunityPost post = new CommunityPost();
            post.setPostId("post-1");
            post.setLanguage("ko");
            post.setContent("?쒖슱 珥ъ쁺吏媛 ?뺣쭚 醫뗭븘??");

            when(communityMapper.findPostById("post-1", 1L)).thenReturn(post);
            when(translationCacheMapper.findLatest(eq("POST"), eq("post-1"), eq("content"), eq("ko"), eq("en"), any()))
                    .thenReturn(null);

            service.translate(user, new TranslationRequest("POST", "post-1", "content", "en"));

            JsonNode payload = objectMapper.readTree(requestBody.get());
            assertThat(payload.get("model").asText()).isEqualTo("gpt-5-nano");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void gmsTranslationResponseSupportsContentPartArrays() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>();
        HttpServer server = startGmsStub(
                requestBody,
                authorization,
                """
                        {"choices":[{"message":{"role":"assistant","content":[{"type":"text","text":"Translated"},{"type":"text","text":" result"}]}}]}
                        """
        );

        try {
            TranslationService service = new TranslationService(
                    translationCacheMapper,
                    communityMapper,
                    placeReviewMapper,
                    userMapper,
                    new LanguageDetectionService(),
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/",
                    "test-gms-key",
                    "gpt-5-nano",
                    "gpt-5.4-mini",
                    1000,
                    1000
            );

            User user = new User();
            user.setUserId(1L);
            user.setLanguage("en");
            CommunityPost post = new CommunityPost();
            post.setPostId("post-1");
            post.setLanguage("ko");
            post.setContent("?쒖슱 珥ъ쁺吏媛 ?뺣쭚 醫뗭븘??");

            when(communityMapper.findPostById("post-1", 1L)).thenReturn(post);
            when(translationCacheMapper.findLatest(eq("POST"), eq("post-1"), eq("content"), eq("ko"), eq("en"), any()))
                    .thenReturn(null);

            TranslationResponse response = service.translate(user, new TranslationRequest("POST", "post-1", "content", "en"));

            assertThat(response.translatedText()).isEqualTo("Translated result");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void primaryModelFailureFallsBackToConfiguredModel() throws Exception {
        AtomicReference<String> requestBodies = new AtomicReference<>("");
        AtomicReference<String> authorization = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api.openai.com/v1/chat/completions", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            requestBodies.set(requestBodies.get() + body + "\n---\n");
            JsonNode payload = objectMapper.readTree(body);
            String model = payload.get("model").asText();
            if ("gpt-5-nano".equals(model)) {
                byte[] response = "{\"error\":{\"message\":\"model not available\"}}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(400, response.length);
                exchange.getResponseBody().write(response);
            } else {
                byte[] response = """
                        {"choices":[{"message":{"role":"assistant","content":"Fallback translated result"}}]}
                        """.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
            }
            exchange.close();
        });
        server.start();

        try {
            TranslationService service = new TranslationService(
                    translationCacheMapper,
                    communityMapper,
                    placeReviewMapper,
                    userMapper,
                    new LanguageDetectionService(),
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/",
                    "test-gms-key",
                    "gpt-5-nano",
                    "gpt-5.4-mini",
                    1000,
                    1000
            );

            User user = new User();
            user.setUserId(1L);
            user.setLanguage("en");
            CommunityPost post = new CommunityPost();
            post.setPostId("post-1");
            post.setLanguage("ko");
            post.setContent("??뽰뒻 ?Ρ딆겫筌왖揶쎛 ?類ｌ춾 ?ル뿭釉??");

            when(communityMapper.findPostById("post-1", 1L)).thenReturn(post);
            when(translationCacheMapper.findLatest(eq("POST"), eq("post-1"), eq("content"), eq("ko"), eq("en"), any()))
                    .thenReturn(null);

            TranslationResponse response = service.translate(user, new TranslationRequest("POST", "post-1", "content", "en"));

            assertThat(response.translatedText()).isEqualTo("Fallback translated result");
            assertThat(requestBodies.get()).contains("\"model\":\"gpt-5-nano\"");
            assertThat(requestBodies.get()).contains("\"model\":\"gpt-5.4-mini\"");

            ArgumentCaptor<TranslationCache> cacheCaptor = ArgumentCaptor.forClass(TranslationCache.class);
            verify(translationCacheMapper).insert(cacheCaptor.capture());
            assertThat(cacheCaptor.getValue().getModel()).isEqualTo("gpt-5.4-mini");
        } finally {
            server.stop(0);
        }
    }

    private HttpServer startGmsStub(AtomicReference<String> requestBody, AtomicReference<String> authorization, String responseJson) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api.openai.com/v1/chat/completions", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        return server;
    }
}
