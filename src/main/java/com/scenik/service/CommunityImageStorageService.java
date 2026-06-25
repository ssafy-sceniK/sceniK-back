package com.scenik.service;

import com.scenik.dto.CommunityDtos.ImageUploadDto;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityImageStorageService {
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg",
            "jpeg",
            "png",
            "webp",
            "gif"
    );

    private final Path uploadDirectory;

    public CommunityImageStorageService(@Value("${scenik.upload-dir:uploads}") String uploadRoot) {
        this.uploadDirectory = Paths.get(uploadRoot, "community")
                .toAbsolutePath()
                .normalize();
    }

    public ImageUploadDto store(MultipartFile file) {
        validate(file);
        try {
            Files.createDirectories(uploadDirectory);
            String extension = extensionFor(file);
            String fileName = UUID.randomUUID() + extension;
            Path target = uploadDirectory.resolve(fileName).normalize();
            if (!target.startsWith(uploadDirectory)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 파일 이름입니다.");
            }
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new ImageUploadDto("/api/uploads/community/" + fileName, fileName);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지를 업로드하지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 이미지를 선택해 주세요.");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "5MB 이하의 이미지를 선택해 주세요.");
        }
        String contentType = normalizedContentType(file);
        if (!ALLOWED_CONTENT_TYPES.contains(contentType) && !ALLOWED_EXTENSIONS.contains(extensionName(file))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다. 다른 이미지를 선택해 주세요.");
        }
    }

    private String extensionFor(MultipartFile file) {
        String contentType = normalizedContentType(file);
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> switch (extensionName(file)) {
                case "png" -> ".png";
                case "webp" -> ".webp";
                case "gif" -> ".gif";
                default -> ".jpg";
            };
        };
    }

    private String normalizedContentType(MultipartFile file) {
        return file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
    }

    private String extensionName(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        int index = name.lastIndexOf('.');
        return index >= 0 && index + 1 < name.length() ? name.substring(index + 1) : "";
    }
}
