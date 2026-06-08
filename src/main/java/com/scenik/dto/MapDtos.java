package com.scenik.dto;

import java.util.List;

public final class MapDtos {
    private MapDtos() {
    }

    public record MapPlaceDto(
            String id,
            String name,
            String address,
            Double latitude,
            Double longitude,
            String category
    ) {
    }

    public record MapResultDto(
            String provider,
            Boolean configured,
            String message,
            List<MapPlaceDto> results
    ) {
        public static MapResultDto empty(String message) {
            return new MapResultDto("KAKAO", false, message, List.of());
        }
    }
}
