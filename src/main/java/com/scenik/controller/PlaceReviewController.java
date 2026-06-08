package com.scenik.controller;

import com.scenik.dto.ReviewDtos.ReviewDto;
import com.scenik.dto.ReviewDtos.ReviewRequest;
import com.scenik.service.AuthService;
import com.scenik.service.PlaceReviewService;
import com.scenik.util.ListQuerySupport;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlaceReviewController {
    private final AuthService authService;
    private final PlaceReviewService placeReviewService;

    public PlaceReviewController(AuthService authService, PlaceReviewService placeReviewService) {
        this.authService = authService;
        this.placeReviewService = placeReviewService;
    }

    @GetMapping("/api/places/{placeId}/reviews")
    public ResponseEntity<List<ReviewDto>> placeReviews(
            @PathVariable("placeId") String placeId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        ListQuerySupport.validateSort(sort, Set.of("latest", "rating"));
        List<ReviewDto> reviews = placeReviewService.listByPlace(placeId).stream()
                .sorted(reviewComparator(ListQuerySupport.normalizeSort(sort)))
                .toList();
        List<ReviewDto> paged = ListQuerySupport.paginate(reviews, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(reviews.size()))
                .body(paged);
    }

    @PostMapping("/api/places/{placeId}/reviews")
    public ReviewDto createReview(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId,
            @RequestBody ReviewRequest request
    ) {
        return placeReviewService.create(authService.requireUser(authorization), placeId, request);
    }

    @PatchMapping("/api/places/{placeId}/reviews/{reviewId}")
    public ReviewDto updateReview(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId,
            @PathVariable("reviewId") Long reviewId,
            @RequestBody ReviewRequest request
    ) {
        return placeReviewService.update(authService.requireUser(authorization), placeId, reviewId, request);
    }

    @DeleteMapping("/api/places/{placeId}/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId,
            @PathVariable("reviewId") Long reviewId
    ) {
        placeReviewService.delete(authService.requireUser(authorization), placeId, reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/users/me/reviews")
    public ResponseEntity<List<ReviewDto>> myReviews(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        ListQuerySupport.validateSort(sort, Set.of("latest", "rating"));
        List<ReviewDto> reviews = placeReviewService.listMine(authService.requireUser(authorization)).stream()
                .sorted(reviewComparator(ListQuerySupport.normalizeSort(sort)))
                .toList();
        List<ReviewDto> paged = ListQuerySupport.paginate(reviews, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(reviews.size()))
                .body(paged);
    }

    private Comparator<ReviewDto> reviewComparator(String sort) {
        if ("rating".equals(sort)) {
            return Comparator.comparingDouble((ReviewDto review) -> review.rating() == null ? 0 : review.rating())
                    .reversed()
                    .thenComparing(Comparator.comparing(ReviewDto::createdAt, Comparator.nullsLast(Comparator.reverseOrder())));
        }
        return Comparator.comparing(ReviewDto::createdAt, Comparator.nullsLast(Comparator.reverseOrder()));
    }
}
