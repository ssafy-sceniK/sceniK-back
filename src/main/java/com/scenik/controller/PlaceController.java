package com.scenik.controller;

import com.scenik.dto.ApiResponse;
import com.scenik.dto.PlaceDto;
import com.scenik.service.AuthService;
import com.scenik.service.PlaceService;
import com.scenik.util.ListQuerySupport;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<List<PlaceDto>>> list(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "theme", required = false) String theme,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        ListQuerySupport.validateSort(sort, Set.of("popular", "name", "region"));
        List<PlaceDto> places = placeService.getPlaces(region, district, firstText(category, theme), keyword, authService.optionalUserId(authorization));
        places = sortPlaces(places, ListQuerySupport.normalizeSort(sort));
        List<PlaceDto> paged = ListQuerySupport.paginate(places, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(places.size()))
                .body(ApiResponse.ok(paged));
    }

    @GetMapping("/nearby")
    public ApiResponse<List<PlaceDto>> nearby(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "radiusKm", required = false) Double radiusKm,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return ApiResponse.ok(placeService.getNearbyPlaces(latitude, longitude, radiusKm, limit, authService.optionalUserId(authorization)));
    }

    @GetMapping("/{placeId}")
    public ApiResponse<PlaceDto> detail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId
    ) {
        var place = placeService.getPlace(placeId, authService.optionalUserId(authorization));
        if (place == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place was not found.");
        }
        return ApiResponse.ok(place);
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

    private List<PlaceDto> sortPlaces(List<PlaceDto> places, String sort) {
        Comparator<PlaceDto> byName = Comparator.comparing(PlaceDto::placeName, Comparator.nullsLast(String::compareToIgnoreCase));
        return switch (sort) {
            case "popular" -> places.stream()
                    .sorted(Comparator.comparingInt((PlaceDto place) -> place.likeCount() == null ? 0 : place.likeCount()).reversed()
                            .thenComparing(byName))
                    .toList();
            case "name" -> places.stream()
                    .sorted(byName)
                    .toList();
            case "region" -> places.stream()
                    .sorted(Comparator.comparing(PlaceDto::region, Comparator.nullsLast(String::compareToIgnoreCase))
                            .thenComparing(byName))
                    .toList();
            default -> places;
        };
    }
}
