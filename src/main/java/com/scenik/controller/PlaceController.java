package com.scenik.controller;

import com.scenik.dto.PlaceDto;
import com.scenik.service.AuthService;
import com.scenik.service.PlaceService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/places")
public class PlaceController {
    private final AuthService authService;
    private final PlaceService placeService;

    public PlaceController(AuthService authService, PlaceService placeService) {
        this.authService = authService;
        this.placeService = placeService;
    }

    @GetMapping
    public List<PlaceDto> list(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "theme", required = false) String theme,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return placeService.getPlaces(region, district, firstText(category, theme), keyword, authService.optionalUserId(authorization));
    }

    @GetMapping("/nearby")
    public List<PlaceDto> nearby(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "radiusKm", required = false) Double radiusKm,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return placeService.getNearbyPlaces(latitude, longitude, radiusKm, limit, authService.optionalUserId(authorization));
    }

    @GetMapping("/{placeId}")
    public PlaceDto detail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId
    ) {
        var place = placeService.getPlace(placeId, authService.optionalUserId(authorization));
        if (place == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place was not found.");
        }
        return place;
    }

    private String firstText(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return null;
    }
}
