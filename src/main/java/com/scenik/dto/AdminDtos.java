package com.scenik.dto;

import java.util.List;

public final class AdminDtos {
    private AdminDtos() {
    }

    public record ContentUpsertRequest(
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
            List<String> tags
    ) {
    }

    public record PlaceUpsertRequest(
            String placeId,
            String name,
            String placeName,
            String address,
            String region,
            String district,
            String category,
            String description,
            String openingHours,
            String closedDays,
            String phoneNumber,
            String thumbnailUrl,
            String imageUrl,
            String indoorOutdoor,
            Boolean parkingAvailable,
            Double latitude,
            Double longitude
    ) {
    }
}
