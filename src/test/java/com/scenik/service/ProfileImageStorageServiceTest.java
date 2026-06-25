package com.scenik.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class ProfileImageStorageServiceTest {
    @TempDir
    Path uploadRoot;

    @Test
    void acceptsJpgUnderFiveMegabytes() {
        ProfileImageStorageService service = new ProfileImageStorageService(uploadRoot.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                new byte[3 * 1024 * 1024]
        );

        String imageUrl = service.store(file);

        assertThat(imageUrl).startsWith("/api/uploads/profile/");
        String fileName = imageUrl.substring("/api/uploads/profile/".length());
        assertThat(Files.exists(uploadRoot.resolve("profile").resolve(fileName))).isTrue();
    }

    @Test
    void acceptsUppercaseJpgExtensionWhenMimeTypeIsEmpty() {
        ProfileImageStorageService service = new ProfileImageStorageService(uploadRoot.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "PROFILE.JPG",
                "",
                new byte[1024]
        );

        String imageUrl = service.store(file);

        assertThat(imageUrl).endsWith(".jpg");
    }

    @Test
    void rejectsImageOverFiveMegabytes() {
        ProfileImageStorageService service = new ProfileImageStorageService(uploadRoot.toString());
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
