package com.scenik.dto;

import com.scenik.domain.CandidatePlace;
import com.scenik.domain.RoutePlace;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class RouteDtos {
    private RouteDtos() {
    }

    public record AiRouteRequest(
            String contentId,
            List<String> contentIds,
            String region,
            List<String> regions,
            @Min(value = 1, message = "여행 일수는 1일 이상이어야 합니다.")
            Integer durationDays,
            LocalDate startDate,
            LocalDate endDate,
            String dateRangeLabel,
            String transport,
            String companion,
            String style,
            List<String> travelStyles,
            Integer budgetMin,
            Integer budgetMax,
            String budget,
            String hotelAddress
    ) {
    }

    public record AiRouteDto(
            String generationId,
            String routeId,
            String title,
            String summary,
            String contentId,
            List<String> contentIds,
            String contentTitle,
            String region,
            List<String> regions,
            Integer durationDays,
            Integer requestedDurationDays,
            LocalDate startDate,
            LocalDate endDate,
            String dateRangeLabel,
            String transport,
            String companion,
            String style,
            List<String> travelStyles,
            Integer budgetMin,
            Integer budgetMax,
            String budget,
            String trustMessage,
            String sourceType,
            Boolean isPublic,
            List<@Valid CandidatePlaceDto> candidatePlaces,
            List<@Valid RouteDayDto> days,
            List<@Valid RouteStopDto> places,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record CandidatePlaceDto(
            @NotBlank(message = "장소를 선택해 주세요.")
            String placeId,
            String name,
            String region,
            String category,
            String address,
            Double latitude,
            Double longitude,
            String relatedContentId,
            String relatedContentTitle,
            String sceneDescription,
            Boolean verified,
            String thumbnailUrl,
            String imageUrl
    ) {
        public static CandidatePlaceDto from(CandidatePlace place) {
            return new CandidatePlaceDto(
                    place.getPlaceId(),
                    place.getName(),
                    place.getRegion(),
                    place.getCategory(),
                    place.getAddress(),
                    place.getLatitude(),
                    place.getLongitude(),
                    place.getRelatedContentId(),
                    place.getRelatedContentTitle(),
                    place.getSceneDescription(),
                    Boolean.TRUE.equals(place.getVerified()),
                    place.getImageUrl(),
                    place.getImageUrl()
            );
        }

        public static CandidatePlaceDto from(RoutePlace place) {
            return new CandidatePlaceDto(
                    place.getPlaceId(),
                    place.getPlaceName(),
                    place.getRegion(),
                    place.getCategory(),
                    place.getAddress(),
                    place.getLatitude(),
                    place.getLongitude(),
                    place.getRelatedContentId(),
                    place.getRelatedContentTitle(),
                    place.getSceneDescription(),
                    true,
                    place.getImageUrl(),
                    place.getImageUrl()
            );
        }
    }

    public record RouteDayDto(
            @Min(value = 1, message = "여행 일수는 1일 이상이어야 합니다.")
            Integer dayNumber,
            String estimatedTravelTime,
            List<@Valid RouteStopDto> places
    ) {
    }

    public record RouteStopDto(
            String routePlaceId,
            @NotBlank(message = "장소를 선택해 주세요.")
            String placeId,
            String name,
            String placeName,
            @Min(value = 1, message = "여행 일수는 1일 이상이어야 합니다.")
            Integer dayNumber,
            @Min(value = 1, message = "방문 순서는 1 이상이어야 합니다.")
            Integer visitOrder,
            String address,
            Double latitude,
            Double longitude,
            String thumbnailUrl,
            String category,
            String recommendedTime,
            String recommendReason,
            String reason,
            Integer estimatedStayMinutes,
            Integer estimatedTravelMinutes,
            String travelTip
    ) {
        public static RouteStopDto from(RoutePlace place) {
            return new RouteStopDto(
                    place.getRoutePlaceId(),
                    place.getPlaceId(),
                    place.getPlaceName(),
                    place.getPlaceName(),
                    place.getDayNumber(),
                    place.getVisitOrder(),
                    place.getAddress(),
                    place.getLatitude(),
                    place.getLongitude(),
                    place.getImageUrl(),
                    place.getCategory(),
                    place.getRecommendedTime(),
                    place.getReason(),
                    place.getReason(),
                    60,
                    25,
                    place.getTravelTip()
            );
        }
    }

    public record RoutePlaceRequest(
            @NotBlank(message = "장소를 선택해 주세요.")
            String placeId,
            @Min(value = 1, message = "여행 일수는 1일 이상이어야 합니다.")
            Integer dayNumber,
            @Min(value = 1, message = "방문 순서는 1 이상이어야 합니다.")
            Integer visitOrder,
            String recommendedTime,
            String reason,
            String travelTip
    ) {
    }

    public record RoutePlaceReorderItem(String routePlaceId, Integer dayNumber, Integer visitOrder) {
    }

    public record RoutePlaceReorderRequest(List<RoutePlaceReorderItem> places) {
    }
}
