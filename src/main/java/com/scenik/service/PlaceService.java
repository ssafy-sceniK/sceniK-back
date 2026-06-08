package com.scenik.service;

import com.scenik.dto.PlaceDto;
import com.scenik.dto.RelatedContentDto;
import com.scenik.mapper.ContentMapper;
import com.scenik.mapper.PlaceMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PlaceService {
    private final PlaceMapper placeMapper;
    private final ContentMapper contentMapper;

    public PlaceService(PlaceMapper placeMapper, ContentMapper contentMapper) {
        this.placeMapper = placeMapper;
        this.contentMapper = contentMapper;
    }

    public List<PlaceDto> getPlaces(String region, String district, String category, String keyword, Long userId) {
        return placeMapper.findPlaces(region, district, category, keyword, userId).stream().map(PlaceDto::from).toList();
    }

    public List<PlaceDto> getNearbyPlaces(Double latitude, Double longitude, Double radiusKm, Integer limit, Long userId) {
        if (latitude == null || longitude == null) {
            return List.of();
        }
        double normalizedRadius = radiusKm == null || radiusKm <= 0 ? 5.0 : Math.min(radiusKm, 50.0);
        int normalizedLimit = limit == null || limit < 1 ? 20 : Math.min(limit, 100);
        return placeMapper.findNearby(latitude, longitude, normalizedRadius, normalizedLimit, userId).stream()
                .map(PlaceDto::from)
                .toList();
    }

    public PlaceDto getPlace(String placeId, Long userId) {
        var place = placeMapper.findById(placeId, userId);
        if (place == null) return null;
        List<RelatedContentDto> relatedContents = contentMapper.findByPlaceId(placeId, userId).stream()
                .map(RelatedContentDto::from)
                .toList();
        return PlaceDto.from(place, relatedContents);
    }
}
