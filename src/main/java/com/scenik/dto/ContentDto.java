package com.scenik.dto;

import com.scenik.domain.Content;
import java.util.Arrays;
import java.util.List;

public record ContentDto(
        String contentId,
        String title,
        String category,
        String subcategory,
        Integer releaseYear,
        String status,
        String description,
        String castNames,
        String thumbnailUrl,
        Boolean isPopular,
        String country,
        String ottPlatform,
        List<String> tags,
        Integer likeCount,
        Integer viewCount,
        Integer relatedPlaceCount,
        Boolean isLiked,
        String originalTitle,
        String originalCategory,
        String originalSubcategory,
        String originalDescription,
        String originalCastNames,
        List<String> originalTags,
        String locale
) {
    public static ContentDto from(Content content) {
        List<String> tags = splitTags(content.getTags());
        return new ContentDto(
                content.getContentId(),
                content.getTitle(),
                content.getCategory(),
                content.getSubcategory(),
                content.getReleaseYear(),
                content.getStatus(),
                content.getDescription(),
                content.getCastNames(),
                content.getThumbnailUrl(),
                Boolean.TRUE.equals(content.getPopular()),
                content.getCountry(),
                content.getOttPlatform(),
                tags,
                content.getLikeCount() == null ? 0 : content.getLikeCount(),
                content.getViewCount() == null ? 0 : content.getViewCount(),
                content.getRelatedPlaceCount() == null ? 0 : content.getRelatedPlaceCount(),
                Boolean.TRUE.equals(content.getLiked()),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                "ko"
        );
    }

    public static List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) return List.of();
        return Arrays.stream(tags.split(",")).map(String::trim).filter(item -> !item.isBlank()).toList();
    }
}
