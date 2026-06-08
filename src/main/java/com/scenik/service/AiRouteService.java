package com.scenik.service;

import com.scenik.domain.CandidatePlace;
import com.scenik.dto.RouteDtos.AiRouteDto;
import com.scenik.dto.RouteDtos.AiRouteRequest;
import com.scenik.dto.RouteDtos.CandidatePlaceDto;
import com.scenik.dto.RouteDtos.RouteDayDto;
import com.scenik.dto.RouteDtos.RouteStopDto;
import com.scenik.mapper.AiRouteMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiRouteService {
    private final AiRouteMapper aiRouteMapper;
    private final Map<String, GenerationState> generationCache = new ConcurrentHashMap<>();

    public AiRouteService(AiRouteMapper aiRouteMapper) {
        this.aiRouteMapper = aiRouteMapper;
    }

    public AiRouteDto generateRoute(AiRouteRequest request) {
        AiRouteRequest normalizedRequest = request == null ? new AiRouteRequest(null, List.of(), null, 1, null, null, null, null, null, null, null, null, null) : request;
        String generationId = "gen-" + UUID.randomUUID();
        generationCache.put(generationId, new GenerationState(normalizedRequest, new AtomicInteger(0)));
        return generateRoute(normalizedRequest, generationId, 0);
    }

    public AiRouteDto regenerateRoute(String generationId) {
        GenerationState state = generationCache.get(generationId);
        if (state == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AI route generation was not found.");
        }
        int offset = state.regenerateCount().incrementAndGet();
        return generateRoute(state.request(), generationId, offset);
    }

    private AiRouteDto generateRoute(AiRouteRequest request, String generationId, int offset) {
        List<String> contentIds = normalizeContentIds(request);
        int requestedDays = Math.max(1, request.durationDays() == null ? 1 : request.durationDays());
        int targetCount = Math.max(6, requestedDays * 2);
        List<CandidatePlace> candidates = deduplicate(aiRouteMapper.findCandidatePlaces(contentIds, request.region(), targetCount));
        boolean usedRegionFallback = false;
        if (candidates.isEmpty() && request.region() != null && !request.region().isBlank()) {
            candidates = deduplicate(aiRouteMapper.findCandidatePlaces(contentIds, null, targetCount));
            usedRegionFallback = true;
        }
        if (candidates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No verified candidate places were found.");
        }
        if (offset > 0 && candidates.size() > 1) {
            Collections.rotate(candidates, -(offset % candidates.size()));
        }

        int dayCount = Math.max(1, Math.min(requestedDays, candidates.size()));
        String region = usedRegionFallback ? candidates.get(0).getRegion() : firstText(request.region(), candidates.get(0).getRegion());
        String contentTitle = candidates.get(0).getRelatedContentTitle();
        String style = firstText(request.style(), "scenic");
        List<RouteDayDto> days = buildDays(candidates, dayCount, style);
        List<CandidatePlaceDto> candidateDtos = candidates.stream().map(CandidatePlaceDto::from).toList();
        List<RouteStopDto> flatPlaces = days.stream().flatMap(day -> day.places().stream()).toList();

        // TODO: Replace this deterministic DB-backed selection with FastAPI/RAG ranking once the RAG service is available.
        return new AiRouteDto(
                generationId,
                generationId,
                contentTitle + " " + region + " " + style + " route",
                "A verified-place sceniK route generated from DB-backed content/place mappings.",
                contentIds.isEmpty() ? null : contentIds.get(0),
                contentIds,
                contentTitle,
                region,
                dayCount,
                requestedDays,
                request.startDate(),
                request.endDate(),
                request.dateRangeLabel(),
                request.transport(),
                request.companion(),
                request.style(),
                request.budgetMin(),
                request.budgetMax(),
                request.budget(),
                "Only verified places from the sceniK database candidate list are used.",
                "AI",
                false,
                candidateDtos,
                days,
                flatPlaces,
                null,
                null
        );
    }

    private List<RouteDayDto> buildDays(List<CandidatePlace> candidates, int dayCount, String style) {
        List<List<CandidatePlace>> groups = new ArrayList<>();
        for (int i = 0; i < dayCount; i++) {
            groups.add(new ArrayList<>());
        }
        candidates.subList(0, Math.max(dayCount, Math.min(candidates.size(), dayCount * 3))).forEach(place -> {
            int index = candidates.indexOf(place) % dayCount;
            groups.get(index).add(place);
        });

        List<RouteDayDto> days = new ArrayList<>();
        for (int dayIndex = 0; dayIndex < groups.size(); dayIndex++) {
            int dayNumber = dayIndex + 1;
            List<RouteStopDto> stops = new ArrayList<>();
            List<CandidatePlace> places = groups.get(dayIndex);
            for (int placeIndex = 0; placeIndex < places.size(); placeIndex++) {
                CandidatePlace place = places.get(placeIndex);
                stops.add(new RouteStopDto(
                        null,
                        place.getPlaceId(),
                        place.getName(),
                        place.getName(),
                        dayNumber,
                        placeIndex + 1,
                        place.getAddress(),
                        place.getLatitude(),
                        place.getLongitude(),
                        place.getImageUrl(),
                        recommendedTime(placeIndex),
                        place.getRelatedContentTitle() + " connection and " + style + " travel conditions match this verified place.",
                        place.getRelatedContentTitle() + " connection and " + style + " travel conditions match this verified place.",
                        60,
                        placeIndex == 0 ? 0 : 25,
                        "Check opening hours and local transit before visiting " + place.getRegion() + "."
                ));
            }
            days.add(new RouteDayDto(dayNumber, dayNumber == 1 ? "About 58 min" : "About 74 min", stops));
        }
        return days;
    }

    private List<String> normalizeContentIds(AiRouteRequest request) {
        LinkedHashMap<String, Boolean> ids = new LinkedHashMap<>();
        if (request.contentId() != null && !request.contentId().isBlank()) {
            ids.put(request.contentId(), true);
        }
        if (request.contentIds() != null) {
            request.contentIds().stream().filter(id -> id != null && !id.isBlank()).forEach(id -> ids.put(id, true));
        }
        return new ArrayList<>(ids.keySet());
    }

    private List<CandidatePlace> deduplicate(List<CandidatePlace> places) {
        LinkedHashMap<String, CandidatePlace> byId = new LinkedHashMap<>();
        for (CandidatePlace place : places) {
            byId.putIfAbsent(place.getPlaceId(), place);
        }
        return new ArrayList<>(byId.values());
    }

    private String recommendedTime(int index) {
        if (index == 0) return "10:00";
        if (index == 1) return "14:00";
        return "17:30";
    }

    private String firstText(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? fallback : preferred;
    }

    private record GenerationState(AiRouteRequest request, AtomicInteger regenerateCount) {
    }
}
