package com.scenik.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public final class LikeVisitDtos {
    private LikeVisitDtos() {
    }

    public record LikesDto(List<String> contentIds, List<String> placeIds, Integer contentCount, Integer placeCount) {
    }

    public record VisitStatusRequest(@NotBlank(message = "방문 상태를 선택해 주세요.") String status) {
    }

    public record VisitStatusDto(String placeId, String status) {
    }
}
