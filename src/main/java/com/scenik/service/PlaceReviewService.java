package com.scenik.service;

import com.scenik.domain.PlaceReview;
import com.scenik.domain.User;
import com.scenik.dto.ReviewDtos.ReviewDto;
import com.scenik.dto.ReviewDtos.ReviewRequest;
import com.scenik.mapper.PlaceMapper;
import com.scenik.mapper.PlaceReviewMapper;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlaceReviewService {
    private final PlaceReviewMapper placeReviewMapper;
    private final PlaceMapper placeMapper;

    public PlaceReviewService(PlaceReviewMapper placeReviewMapper, PlaceMapper placeMapper) {
        this.placeReviewMapper = placeReviewMapper;
        this.placeMapper = placeMapper;
    }

    public List<ReviewDto> listByPlace(String placeId) {
        requirePlace(placeId);
        return placeReviewMapper.findByPlaceId(placeId).stream().map(ReviewDto::from).toList();
    }

    public List<ReviewDto> listMine(User user) {
        return placeReviewMapper.findByUserId(user.getUserId()).stream().map(ReviewDto::from).toList();
    }

    public ReviewDto create(User user, String placeId, ReviewRequest request) {
        requirePlace(placeId);
        validate(request);
        if (placeReviewMapper.findByPlaceIdAndUserId(placeId, user.getUserId()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Place review already exists.");
        }

        PlaceReview review = new PlaceReview();
        review.setPlaceId(placeId);
        review.setUserId(user.getUserId());
        review.setRating(request.rating());
        review.setContent(request.content().trim());
        placeReviewMapper.insert(review);
        return ReviewDto.from(placeReviewMapper.findById(review.getReviewId()));
    }

    public ReviewDto update(User user, String placeId, Long reviewId, ReviewRequest request) {
        requirePlace(placeId);
        validate(request);
        PlaceReview existing = requireReview(reviewId);
        requireOwner(user, existing);
        if (!placeId.equals(existing.getPlaceId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review was not found.");
        }

        existing.setRating(request.rating());
        existing.setContent(request.content().trim());
        placeReviewMapper.update(existing);
        return ReviewDto.from(placeReviewMapper.findById(reviewId));
    }

    public void delete(User user, String placeId, Long reviewId) {
        requirePlace(placeId);
        PlaceReview existing = requireReview(reviewId);
        requireOwner(user, existing);
        if (!placeId.equals(existing.getPlaceId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review was not found.");
        }
        placeReviewMapper.delete(reviewId);
    }

    private void validate(ReviewRequest request) {
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review content is required.");
        }
        if (request.rating() != null && (request.rating() < 0 || request.rating() > 5)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review rating must be between 0 and 5.");
        }
    }

    private void requirePlace(String placeId) {
        if (placeMapper.findByIdForWrite(placeId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place was not found.");
        }
    }

    private PlaceReview requireReview(Long reviewId) {
        PlaceReview review = placeReviewMapper.findById(reviewId);
        if (review == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review was not found.");
        }
        return review;
    }

    private void requireOwner(User user, PlaceReview review) {
        if (!user.getUserId().equals(review.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Review owner does not match.");
        }
    }
}
