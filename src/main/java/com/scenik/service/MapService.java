package com.scenik.service;

import com.scenik.dto.MapDtos.MapResultDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MapService {
    private final String kakaoRestApiKey;

    public MapService(@Value("${kakao.rest-api-key:}") String kakaoRestApiKey) {
        this.kakaoRestApiKey = kakaoRestApiKey;
    }

    public MapResultDto geocode(String query) {
        return fallback("Kakao geocode integration is not enabled for this environment.", query);
    }

    public MapResultDto reverseGeocode(Double latitude, Double longitude) {
        return fallback("Kakao reverse geocode integration is not enabled for this environment.", latitude, longitude);
    }

    public MapResultDto localSearch(String query, Double latitude, Double longitude, Integer radius) {
        return fallback("Kakao local search integration is not enabled for this environment.", query, latitude, longitude, radius);
    }

    private MapResultDto fallback(String message, Object... ignoredInputs) {
        if (kakaoRestApiKey == null || kakaoRestApiKey.isBlank()) {
            return MapResultDto.empty(message + " Configure kakao.rest-api-key to enable live Kakao requests.");
        }
        // TODO: Call Kakao Local REST APIs from the backend only. Frontend must never call Kakao directly.
        return new MapResultDto("KAKAO", true, message + " Week 2 skeleton returns safe empty results.", java.util.List.of());
    }
}
