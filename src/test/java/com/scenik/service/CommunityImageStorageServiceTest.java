package com.scenik.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class CommunityImageStorageServiceTest {
    @TempDir
    Path uploadRoot;

    @Test
    void acceptsJpgUnderFiveMegabytes() {
        CommunityImageStorageService service = new CommunityImageStorageService(uploadRoot.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[3 * 1024 * 1024]
        );

        var uploaded = service.store(file);

        assertThat(uploaded.imageUrl()).startsWith("/api/uploads/community/");
        assertThat(Files.exists(uploadRoot.resolve("community").resolve(uploaded.fileName()))).isTrue();
    }

    @Test
    void acceptsUppercaseJpgExtensionWhenMimeTypeIsEmpty() {
        CommunityImageStorageService service = new CommunityImageStorageService(uploadRoot.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "PHOTO.JPG",
                "",
                new byte[1024]
        );

        var uploaded = service.store(file);

        assertThat(uploaded.fileName()).endsWith(".jpg");
    }

    @Test
    void rejectsImageOverFiveMegabytes() {
        CommunityImageStorageService service = new CommunityImageStorageService(uploadRoot.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                new byte[(5 * 1024 * 1024) + 1]
        );

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("5MB");
    }
}
