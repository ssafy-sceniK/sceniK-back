package com.scenik.mapper;

import com.scenik.domain.PlaceReview;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlaceReviewMapper {
    List<PlaceReview> findByPlaceId(@Param("placeId") String placeId);
    List<PlaceReview> findByUserId(@Param("userId") Long userId);
    PlaceReview findById(@Param("reviewId") Long reviewId);
    PlaceReview findByPlaceIdAndUserId(@Param("placeId") String placeId, @Param("userId") Long userId);
    int insert(PlaceReview review);
    int update(PlaceReview review);
    int delete(@Param("reviewId") Long reviewId);
}
