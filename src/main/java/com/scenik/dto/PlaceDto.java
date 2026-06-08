package com.scenik.dto;

import com.scenik.domain.Place;
import java.util.List;

public record PlaceDto(
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
        Double longitude,
        Integer likeCount,
        Boolean isLiked,
        String visitStatus,
        Integer reviewCount,
        Double averageRating,
        List<RelatedContentDto> relatedContents,
        Boolean verified
) {
    public static PlaceDto from(Place place) {
        return from(place, List.of());
    }

    public static PlaceDto from(Place place, List<RelatedContentDto> relatedContents) {
        return new PlaceDto(
                place.getPlaceId(),
                place.getPlaceName(),
                place.getPlaceName(),
                place.getAddress(),
                place.getRegion(),
                place.getDistrict(),
                place.getCategory(),
                place.getDescription(),
                place.getOpeningHours(),
                place.getClosedDays(),
                place.getPhoneNumber(),
                place.getImageUrl(),
                place.getImageUrl(),
                place.getIndoorOutdoor(),
                Boolean.TRUE.equals(place.getParkingAvailable()),
                place.getLatitude(),
                place.getLongitude(),
                place.getLikeCount() == null ? 0 : place.getLikeCount(),
                Boolean.TRUE.equals(place.getLiked()),
                place.getVisitStatus(),
                place.getReviewCount() == null ? 0 : place.getReviewCount(),
                place.getAverageRating() == null ? 0.0 : place.getAverageRating(),
                relatedContents == null ? List.of() : relatedContents,
                true
        );
    }
}
