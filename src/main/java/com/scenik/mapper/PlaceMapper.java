package com.scenik.mapper;

import com.scenik.domain.Place;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlaceMapper {
    List<Place> findPlaces(@Param("region") String region, @Param("district") String district, @Param("category") String category, @Param("keyword") String keyword, @Param("userId") Long userId);
    List<Place> findNearby(@Param("latitude") Double latitude, @Param("longitude") Double longitude, @Param("radiusKm") Double radiusKm, @Param("limit") Integer limit, @Param("userId") Long userId);
    Place findById(@Param("placeId") String placeId, @Param("userId") Long userId);
    Place findByIdForWrite(@Param("placeId") String placeId);
    int insert(Place place);
    int update(Place place);
    int deleteRoutePlaceReferences(@Param("placeId") String placeId);
    int deletePlaceLikeReferences(@Param("placeId") String placeId);
    int deleteVisitStatusReferences(@Param("placeId") String placeId);
    int deleteContentPlaceReferences(@Param("placeId") String placeId);
    int deleteTagReferences(@Param("placeId") String placeId);
    int delete(@Param("placeId") String placeId);
}
