package com.scenik.dto;

import com.scenik.domain.PlaceReview;
import java.time.LocalDateTime;

public final class ReviewDtos {
    private ReviewDtos() {
    }

    public record ReviewRequest(Double rating, String content) {
    }

    public record ReviewDto(
            Long reviewId,
            String placeId,
            Long userId,
            String nickname,
            Double rating,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static ReviewDto from(PlaceReview review) {
            return new ReviewDto(
                    review.getReviewId(),
                    review.getPlaceId(),
                    review.getUserId(),
                    review.getNickname(),
                    review.getRating(),
                    review.getContent(),
                    review.getCreatedAt(),
                    review.getUpdatedAt()
            );
        }
    }
}
