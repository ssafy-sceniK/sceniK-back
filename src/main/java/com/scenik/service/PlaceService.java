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
    private final SystemLocalizationService localizationService;

    public PlaceService(PlaceMapper placeMapper, ContentMapper contentMapper, SystemLocalizationService localizationService) {
        this.placeMapper = placeMapper;
        this.contentMapper = contentMapper;
        this.localizationService = localizationService;
    }

    public List<PlaceDto> getPlaces(String region, String district, String category, String keyword, Long userId, String acceptLanguage) {
        return placeMapper.findPlaces(region, district, category, keyword, userId).stream()
                .map(PlaceDto::from)
                .map(place -> localizationService.localize(place, acceptLanguage))
                .toList();
    }

    public List<PlaceDto> getNearbyPlaces(Double latitude, Double longitude, Double radiusKm, Integer limit, Long userId, String acceptLanguage) {
        if (latitude == null || longitude == null) {
            return List.of();
        }
        double normalizedRadius = radiusKm == null || radiusKm <= 0 ? 5.0 : Math.min(radiusKm, 50.0);
        int normalizedLimit = limit == null || limit < 1 ? 20 : Math.min(limit, 100);
        return placeMapper.findNearby(latitude, longitude, normalizedRadius, normalizedLimit, userId).stream()
                .map(PlaceDto::from)
                .map(place -> localizationService.localize(place, acceptLanguage))
                .toList();
    }

    public PlaceDto getPlace(String placeId, Long userId, String acceptLanguage) {
        var place = placeMapper.findById(placeId, userId);
        if (place == null) return null;
        List<RelatedContentDto> relatedContents = contentMapper.findByPlaceId(placeId, userId).stream()
                .map(RelatedContentDto::from)
                .map(content -> localizationService.localize(content, acceptLanguage))
                .toList();
        return localizationService.localize(PlaceDto.from(place, relatedContents), acceptLanguage);
    }
}
