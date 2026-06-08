package com.scenik.controller;

import com.scenik.dto.MapDtos.MapResultDto;
import com.scenik.service.MapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maps")
public class MapController {
    private final MapService mapService;

    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/geocode")
    public MapResultDto geocode(
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "query", required = false) String query
    ) {
        return mapService.geocode(firstText(address, query));
    }

    @GetMapping("/reverse-geocode")
    public MapResultDto reverseGeocode(
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude
    ) {
        return mapService.reverseGeocode(latitude, longitude);
    }

    @GetMapping("/local-search")
    public MapResultDto localSearch(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "radius", required = false) Integer radius
    ) {
        return mapService.localSearch(firstText(keyword, query), latitude, longitude, radius);
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
