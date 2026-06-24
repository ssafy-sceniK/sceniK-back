package com.scenik.dto;

import com.scenik.domain.Content;

public record RelatedContentDto(
        String contentId,
        String title,
        String category,
        String thumbnailUrl,
        Integer releaseYear,
        String originalTitle,
        String originalCategory,
        String locale
) {
    public static RelatedContentDto from(Content content) {
        return new RelatedContentDto(
                content.getContentId(),
                content.getTitle(),
                content.getCategory(),
                content.getThumbnailUrl(),
                content.getReleaseYear(),
                null,
                null,
                "ko"
        );
    }
}
