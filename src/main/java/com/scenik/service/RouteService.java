package com.scenik.service;

import com.scenik.domain.Place;
import com.scenik.domain.RoutePlace;
import com.scenik.domain.TravelRoute;
import com.scenik.domain.User;
import com.scenik.dto.RouteDtos.AiRouteDto;
import com.scenik.dto.RouteDtos.CandidatePlaceDto;
import com.scenik.dto.RouteDtos.RouteDayDto;
import com.scenik.dto.RouteDtos.RoutePlaceReorderItem;
import com.scenik.dto.RouteDtos.RoutePlaceReorderRequest;
import com.scenik.dto.RouteDtos.RoutePlaceRequest;
import com.scenik.dto.RouteDtos.RouteStopDto;
import com.scenik.mapper.CommunityMapper;
import com.scenik.mapper.PlaceMapper;
import com.scenik.mapper.RouteMapper;
import com.scenik.mapper.RoutePlaceMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RouteService {
    private static final Logger log = LoggerFactory.getLogger(RouteService.class);

    private final RouteMapper routeMapper;
    private final RoutePlaceMapper routePlaceMapper;
    private final PlaceMapper placeMapper;
    private final CommunityMapper communityMapper;

    public RouteService(RouteMapper routeMapper, RoutePlaceMapper routePlaceMapper, PlaceMapper placeMapper, CommunityMapper communityMapper) {
        this.routeMapper = routeMapper;
        this.routePlaceMapper = routePlaceMapper;
        this.placeMapper = placeMapper;
        this.communityMapper = communityMapper;
    }

    public List<AiRouteDto> listMyRoutes(User user) {
        return listRoutesByUserId(user.getUserId());
    }

    public List<AiRouteDto> listRoutesByUserId(Long userId) {
        return routeMapper.findByUserId(userId).stream().map(this::toDto).toList();
    }

    public AiRouteDto getRoute(Long userId, String routeId) {
        return toDto(requireReadableRoute(userId, routeId));
    }

    @Transactional
    public AiRouteDto createRoute(User user, AiRouteDto request) {
        AiRouteDto normalized = normalizeRouteRequest(request);
        log.info("Route save request userId={} title={} region={} regions={} durationDays={} days={} candidates={} stops={}",
                user.getUserId(),
                normalized == null ? null : normalized.title(),
                normalized == null ? null : normalized.region(),
                normalized == null ? null : normalized.regions(),
                normalized == null ? null : normalized.durationDays(),
                normalized == null || normalized.days() == null ? 0 : normalized.days().size(),
                normalized == null || normalized.candidatePlaces() == null ? 0 : normalized.candidatePlaces().size(),
                normalized == null || normalized.days() == null ? 0 : normalized.days().stream().mapToInt(day -> day.places() == null ? 0 : day.places().size()).sum());
        try {
            validateRouteRequest(normalized);
        } catch (ResponseStatusException error) {
            log.warn("Route save validation failed userId={} status={} reason={}",
                    user.getUserId(),
                    error.getStatusCode(),
                    error.getReason());
            throw error;
        }
        TravelRoute route = toRoute(user, "route-" + UUID.randomUUID(), normalized);
        routeMapper.insert(route);
        for (RouteDayDto day : normalized.days()) {
            for (RouteStopDto stop : day.places()) {
                RoutePlace place = toRoutePlace(route.getRouteId(), stop);
                validateRoutePlace(route, place, null, false);
                routePlaceMapper.insert(place);
            }
        }
        return toDto(routeMapper.findById(route.getRouteId()));
    }

    @Transactional
    public AiRouteDto updateRoute(User user, String routeId, AiRouteDto request) {
        TravelRoute existing = requireOwnedRoute(user, routeId);
        AiRouteDto normalized = normalizeRouteRequest(merge(toDto(existing), request));
        validateRouteRequest(normalized);
        TravelRoute route = toRoute(user, routeId, normalized);
        routeMapper.update(route);
        if (request != null && (request.days() != null || request.places() != null)) {
            routePlaceMapper.deleteByRouteId(routeId);
            for (RouteDayDto day : normalized.days()) {
                for (RouteStopDto stop : day.places()) {
                    RoutePlace place = toRoutePlace(routeId, stop);
                    validateRoutePlace(route, place, null, false);
                    routePlaceMapper.insert(place);
                }
            }
        }
        return toDto(routeMapper.findById(existing.getRouteId()));
    }

    private AiRouteDto merge(AiRouteDto existing, AiRouteDto request) {
        if (request == null) return existing;
        return new AiRouteDto(
                existing.generationId(),
                existing.routeId(),
                request.title() == null ? existing.title() : request.title(),
                request.summary() == null ? existing.summary() : request.summary(),
                request.contentId() == null ? existing.contentId() : request.contentId(),
                request.contentIds() == null ? existing.contentIds() : request.contentIds(),
                request.contentTitle() == null ? existing.contentTitle() : request.contentTitle(),
                request.region() == null ? existing.region() : request.region(),
                request.regions() == null ? existing.regions() : request.regions(),
                request.durationDays() == null ? existing.durationDays() : request.durationDays(),
                request.requestedDurationDays() == null ? existing.requestedDurationDays() : request.requestedDurationDays(),
                request.startDate() == null ? existing.startDate() : request.startDate(),
                request.endDate() == null ? existing.endDate() : request.endDate(),
                request.dateRangeLabel() == null ? existing.dateRangeLabel() : request.dateRangeLabel(),
                request.transport() == null ? existing.transport() : request.transport(),
                request.companion() == null ? existing.companion() : request.companion(),
                request.style() == null ? existing.style() : request.style(),
                request.travelStyles() == null ? existing.travelStyles() : request.travelStyles(),
                request.budgetMin() == null ? existing.budgetMin() : request.budgetMin(),
                request.budgetMax() == null ? existing.budgetMax() : request.budgetMax(),
                request.budget() == null ? existing.budget() : request.budget(),
                request.trustMessage() == null ? existing.trustMessage() : request.trustMessage(),
                request.sourceType() == null ? existing.sourceType() : request.sourceType(),
                request.isPublic() == null ? existing.isPublic() : request.isPublic(),
                request.candidatePlaces() == null ? existing.candidatePlaces() : request.candidatePlaces(),
                request.places() != null && request.days() == null ? null : request.days() == null ? existing.days() : request.days(),
                request.places() == null ? existing.places() : request.places(),
                existing.createdAt(),
                existing.updatedAt()
        );
    }

    @Transactional
    public void deleteRoute(User user, String routeId) {
        TravelRoute route = requireOwnedRoute(user, routeId);
        routeMapper.delete(route.getRouteId());
    }

    @Transactional
    public AiRouteDto addPlace(User user, String routeId, RoutePlaceRequest request) {
        TravelRoute route = requireOwnedRoute(user, routeId);
        RoutePlace place = toRoutePlace(routeId, request);
        validateRoutePlace(route, place, null, true);
        routePlaceMapper.insert(place);
        return toDto(route);
    }

    @Transactional
    public AiRouteDto updatePlace(User user, String routeId, String routePlaceId, RoutePlaceRequest request) {
        TravelRoute route = requireOwnedRoute(user, routeId);
        RoutePlace existing = requireRoutePlace(routeId, routePlaceId);
        RoutePlace place = toRoutePlace(routeId, request);
        place.setRoutePlaceId(existing.getRoutePlaceId());
        validateRoutePlace(route, place, routePlaceId, true);
        routePlaceMapper.update(place);
        return toDto(route);
    }

    @Transactional
    public AiRouteDto deletePlace(User user, String routeId, String routePlaceId) {
        TravelRoute route = requireOwnedRoute(user, routeId);
        requireRoutePlace(routeId, routePlaceId);
        routePlaceMapper.delete(routePlaceId);
        return toDto(route);
    }

    @Transactional
    public AiRouteDto reorderPlaces(User user, String routeId, RoutePlaceReorderRequest request) {
        TravelRoute route = requireOwnedRoute(user, routeId);
        if (request.places() == null || request.places().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reorder places are required.");
        }
        List<RoutePlace> existingPlaces = routePlaceMapper.findByRouteId(routeId);
        Map<String, RoutePlaceReorderItem> requestedById = new LinkedHashMap<>();
        for (RoutePlaceReorderItem item : request.places()) {
            requestedById.put(item.routePlaceId(), item);
        }
        Map<String, String> dayOrders = new LinkedHashMap<>();
        for (RoutePlace existing : existingPlaces) {
            RoutePlaceReorderItem item = requestedById.remove(existing.getRoutePlaceId());
            Integer dayNumber = item == null ? existing.getDayNumber() : item.dayNumber();
            Integer visitOrder = item == null ? existing.getVisitOrder() : item.visitOrder();
            validateDayAndOrder(route, dayNumber, visitOrder);
            String key = dayNumber + ":" + visitOrder;
            if (dayOrders.put(key, existing.getRoutePlaceId()) != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate visitOrder for route day.");
            }
        }
        if (!requestedById.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Route place was not found.");
        }
        for (RoutePlaceReorderItem item : request.places()) {
            validateDayAndOrder(route, item.dayNumber(), item.visitOrder());
        }
        for (RoutePlaceReorderItem item : request.places()) {
            routePlaceMapper.updateOrder(item.routePlaceId(), item.dayNumber(), -item.visitOrder());
        }
        for (RoutePlaceReorderItem item : request.places()) {
            routePlaceMapper.updateOrder(item.routePlaceId(), item.dayNumber(), item.visitOrder());
        }
        return toDto(route);
    }


    private TravelRoute requireReadableRoute(Long userId, String routeId) {
        TravelRoute route = routeMapper.findById(routeId);
        if (route == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Route was not found.");
        }
        if (userId != null && route.getUserId().equals(userId)) {
            return route;
        }
        if (communityMapper.countActivePostsByRouteId(routeId) > 0) {
            return route;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Route is not shared publicly.");
    }

    private TravelRoute requireOwnedRoute(User user, String routeId) {
        TravelRoute route = routeMapper.findById(routeId);
        if (route == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Route was not found.");
        }
        if (!route.getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Route owner does not match.");
        }
        return route;
    }

    private RoutePlace requireRoutePlace(String routeId, String routePlaceId) {
        RoutePlace place = routePlaceMapper.findById(routePlaceId);
        if (place == null || !routeId.equals(place.getRouteId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Route place was not found.");
        }
        return place;
    }

    private void validateRouteRequest(AiRouteDto request) {
        if (request == null || request.days() == null || request.days().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route days are required.");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route title is required.");
        }
        if (request.region() == null || request.region().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route region is required.");
        }
        if (request.durationDays() == null || request.durationDays() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "durationDays must be positive.");
        }
        Map<String, Boolean> candidateIds = new LinkedHashMap<>();
        if (request.candidatePlaces() == null || request.candidatePlaces().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "candidatePlaces are required.");
        }
        for (CandidatePlaceDto candidate : request.candidatePlaces()) {
            if (candidate.placeId() == null || candidate.placeId().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidate placeId is required.");
            }
            if (!Boolean.TRUE.equals(candidate.verified())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidate places must be verified.");
            }
            if (candidateIds.put(candidate.placeId(), true) != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate candidate placeId.");
            }
        }
        Map<String, Boolean> placeIds = new LinkedHashMap<>();
        Map<String, Boolean> dayOrders = new LinkedHashMap<>();
        for (RouteDayDto day : request.days()) {
            if (day.dayNumber() == null || day.dayNumber() < 1 || day.dayNumber() > request.durationDays()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid dayNumber.");
            }
            if (day.places() == null || day.places().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each day must include places.");
            }
            for (RouteStopDto stop : day.places()) {
                if (stop.placeId() == null || stop.placeId().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route stop placeId is required.");
                }
                if (!day.dayNumber().equals(stop.dayNumber())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stop dayNumber must match parent day.");
                }
                if (stop.visitOrder() == null || stop.visitOrder() < 1) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "visitOrder must be positive.");
                }
                if (placeIds.put(stop.placeId(), true) != null) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate placeId in route.");
                }
                if (!candidateIds.containsKey(stop.placeId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route placeId must exist in candidatePlaces.");
                }
                String key = stop.dayNumber() + ":" + stop.visitOrder();
                if (dayOrders.put(key, true) != null) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate visitOrder for route day.");
                }
            }
        }
    }

    private AiRouteDto normalizeRouteRequest(AiRouteDto request) {
        if (request == null) {
            return null;
        }

        List<RouteDayDto> sourceDays = request.days();
        if ((sourceDays == null || sourceDays.isEmpty()) && request.places() != null && !request.places().isEmpty()) {
            sourceDays = groupFlatPlaces(request.places());
        }
        if (sourceDays == null || sourceDays.isEmpty()) {
            return request;
        }

        Map<String, CandidatePlaceDto> candidates = new LinkedHashMap<>();
        if (request.candidatePlaces() != null) {
            for (CandidatePlaceDto candidate : request.candidatePlaces()) {
                if (candidate != null && candidate.placeId() != null && !candidate.placeId().isBlank()) {
                    candidates.putIfAbsent(candidate.placeId(), candidate);
                }
            }
        }

        List<RouteDayDto> normalizedDays = new ArrayList<>();
        for (int dayIndex = 0; dayIndex < sourceDays.size(); dayIndex++) {
            RouteDayDto day = sourceDays.get(dayIndex);
            if (day == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route day is required.");
            }
            Integer dayNumber = day.dayNumber() == null ? dayIndex + 1 : day.dayNumber();
            List<RouteStopDto> stops = new ArrayList<>();
            List<RouteStopDto> sourceStops = day.places() == null ? List.of() : day.places();
            for (int stopIndex = 0; stopIndex < sourceStops.size(); stopIndex++) {
                RouteStopDto stop = normalizeStop(sourceStops.get(stopIndex), dayNumber, stopIndex);
                candidates.putIfAbsent(stop.placeId(), candidateFromPlaceId(stop.placeId()));
                stops.add(stop);
            }
            normalizedDays.add(new RouteDayDto(dayNumber, day.estimatedTravelTime(), stops));
        }

        List<RouteStopDto> flatPlaces = normalizedDays.stream().flatMap(day -> day.places().stream()).toList();
        return new AiRouteDto(
                request.generationId(),
                request.routeId(),
                request.title(),
                request.summary(),
                request.contentId(),
                request.contentIds(),
                request.contentTitle(),
                request.region(),
                request.regions(),
                request.durationDays(),
                request.requestedDurationDays(),
                request.startDate(),
                request.endDate(),
                request.dateRangeLabel(),
                request.transport(),
                request.companion(),
                request.style(),
                request.travelStyles(),
                request.budgetMin(),
                request.budgetMax(),
                request.budget(),
                request.trustMessage(),
                request.sourceType(),
                request.isPublic(),
                candidates.values().stream().toList(),
                normalizedDays,
                flatPlaces,
                request.createdAt(),
                request.updatedAt()
        );
    }

    private List<RouteDayDto> groupFlatPlaces(List<RouteStopDto> places) {
        Map<Integer, List<RouteStopDto>> byDay = new LinkedHashMap<>();
        for (int index = 0; index < places.size(); index++) {
            RouteStopDto stop = places.get(index);
            if (stop == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route stop is required.");
            }
            Integer dayNumber = stop.dayNumber() == null ? 1 : stop.dayNumber();
            byDay.computeIfAbsent(dayNumber, key -> new ArrayList<>()).add(stop);
        }
        return byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new RouteDayDto(entry.getKey(), null, entry.getValue()))
                .toList();
    }

    private RouteStopDto normalizeStop(RouteStopDto stop, Integer dayNumber, int stopIndex) {
        if (stop == null || stop.placeId() == null || stop.placeId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route stop placeId is required.");
        }
        Place place = requireActivePlace(stop.placeId());
        Integer visitOrder = stop.visitOrder() == null ? stopIndex + 1 : stop.visitOrder();
        String name = firstText(stop.name(), firstText(stop.placeName(), place.getPlaceName()));
        String reason = firstText(stop.recommendReason(), firstText(stop.reason(), "Added to saved route."));
        return new RouteStopDto(
                stop.routePlaceId(),
                stop.placeId(),
                name,
                name,
                dayNumber,
                visitOrder,
                firstText(stop.address(), place.getAddress()),
                stop.latitude() == null ? place.getLatitude() : stop.latitude(),
                stop.longitude() == null ? place.getLongitude() : stop.longitude(),
                firstText(stop.thumbnailUrl(), place.getImageUrl()),
                firstText(stop.category(), place.getCategory()),
                firstText(stop.recommendedTime(), "10:00"),
                reason,
                reason,
                stop.estimatedStayMinutes() == null ? 60 : stop.estimatedStayMinutes(),
                stop.estimatedTravelMinutes() == null ? 25 : stop.estimatedTravelMinutes(),
                firstText(stop.travelTip(), "Check opening hours and transit before visiting.")
        );
    }

    private CandidatePlaceDto candidateFromPlaceId(String placeId) {
        Place place = requireActivePlace(placeId);
        return new CandidatePlaceDto(
                place.getPlaceId(),
                place.getPlaceName(),
                place.getRegion(),
                place.getCategory(),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude(),
                null,
                null,
                firstText(place.getDescription(), "Verified sceniK place."),
                true,
                place.getImageUrl(),
                place.getImageUrl()
        );
    }

    private Place requireActivePlace(String placeId) {
        Place place = placeMapper.findByIdForWrite(placeId);
        if (place == null || "DELETED".equalsIgnoreCase(place.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place was not found.");
        }
        return place;
    }

    private void validateRoutePlace(TravelRoute route, RoutePlace place, String excludeRoutePlaceId, boolean checkExisting) {
        requireActivePlace(place.getPlaceId());
        validateDayAndOrder(route, place.getDayNumber(), place.getVisitOrder());
        if (place.getRecommendedTime() == null || place.getRecommendedTime().isBlank()
                || place.getReason() == null || place.getReason().isBlank()
                || place.getTravelTip() == null || place.getTravelTip().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "recommendedTime, reason and travelTip are required.");
        }
        if (checkExisting && routePlaceMapper.existsByRouteAndPlace(route.getRouteId(), place.getPlaceId(), excludeRoutePlaceId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate placeId in route.");
        }
        if (checkExisting && routePlaceMapper.existsByRouteDayOrder(route.getRouteId(), place.getDayNumber(), place.getVisitOrder(), excludeRoutePlaceId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate visitOrder for route day.");
        }
    }

    private void validateDayAndOrder(TravelRoute route, Integer dayNumber, Integer visitOrder) {
        if (dayNumber == null || dayNumber < 1 || dayNumber > route.getDurationDays()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dayNumber is out of bounds.");
        }
        if (visitOrder == null || visitOrder < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "visitOrder must be positive.");
        }
    }

    private TravelRoute toRoute(User user, String routeId, AiRouteDto request) {
        TravelRoute route = new TravelRoute();
        route.setRouteId(routeId);
        route.setUserId(user.getUserId());
        route.setTitle(request.title());
        route.setSummary(request.summary());
        route.setContentId(request.contentId());
        route.setContentIds(join(request.contentIds()));
        route.setContentTitle(request.contentTitle());
        // TODO: Add a dedicated route regions table/column. For now, store selected regions as a comma-separated display string.
        route.setRegion(regionForStorage(request));
        route.setDurationDays(request.durationDays());
        route.setRequestedDurationDays(request.requestedDurationDays());
        route.setStartDate(request.startDate());
        route.setEndDate(request.endDate());
        route.setDateRangeLabel(request.dateRangeLabel());
        route.setTransport(request.transport());
        route.setCompanion(request.companion());
        route.setStyle(styleForStorage(request));
        route.setBudgetMin(request.budgetMin());
        route.setBudgetMax(request.budgetMax());
        route.setBudget(request.budget());
        route.setTrustMessage(request.trustMessage());
        return route;
    }

    private RoutePlace toRoutePlace(String routeId, RouteStopDto stop) {
        RoutePlace place = new RoutePlace();
        place.setRoutePlaceId(stop.routePlaceId() == null || stop.routePlaceId().isBlank() ? "rp-" + UUID.randomUUID() : stop.routePlaceId());
        place.setRouteId(routeId);
        place.setPlaceId(stop.placeId());
        place.setDayNumber(stop.dayNumber());
        place.setVisitOrder(stop.visitOrder());
        place.setRecommendedTime(stop.recommendedTime());
        place.setReason(firstText(stop.recommendReason(), stop.reason()));
        place.setTravelTip(stop.travelTip());
        return place;
    }

    private RoutePlace toRoutePlace(String routeId, RoutePlaceRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route place request is required.");
        }
        RoutePlace place = new RoutePlace();
        place.setRoutePlaceId("rp-" + UUID.randomUUID());
        place.setRouteId(routeId);
        place.setPlaceId(request.placeId());
        place.setDayNumber(request.dayNumber());
        place.setVisitOrder(request.visitOrder());
        place.setRecommendedTime(firstText(request.recommendedTime(), "10:00"));
        place.setReason(firstText(request.reason(), "Added to saved route."));
        place.setTravelTip(firstText(request.travelTip(), "Check opening hours and transit before visiting."));
        return place;
    }

    private AiRouteDto toDto(TravelRoute route) {
        List<RoutePlace> places = routePlaceMapper.findByRouteId(route.getRouteId());
        List<CandidatePlaceDto> candidates = places.stream()
                .collect(LinkedHashMap<String, CandidatePlaceDto>::new, (map, place) -> map.putIfAbsent(place.getPlaceId(), CandidatePlaceDto.from(place)), Map::putAll)
                .values().stream().toList();
        Map<Integer, List<RouteStopDto>> byDay = new LinkedHashMap<>();
        for (RoutePlace place : places) {
            byDay.computeIfAbsent(place.getDayNumber(), key -> new ArrayList<>()).add(RouteStopDto.from(place));
        }
        List<RouteDayDto> days = byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new RouteDayDto(entry.getKey(), entry.getKey() == 1 ? "About 58 min" : "About 74 min",
                        entry.getValue().stream().sorted(Comparator.comparing(RouteStopDto::visitOrder)).toList()))
                .toList();
        List<RouteStopDto> flatPlaces = days.stream().flatMap(day -> day.places().stream()).toList();
        return new AiRouteDto(
                null,
                route.getRouteId(),
                route.getTitle(),
                route.getSummary(),
                route.getContentId(),
                split(route.getContentIds()),
                route.getContentTitle(),
                route.getRegion(),
                split(route.getRegion()),
                route.getDurationDays(),
                route.getRequestedDurationDays(),
                route.getStartDate(),
                route.getEndDate(),
                route.getDateRangeLabel(),
                route.getTransport(),
                route.getCompanion(),
                route.getStyle(),
                splitStyles(route.getStyle()),
                route.getBudgetMin(),
                route.getBudgetMax(),
                route.getBudget(),
                route.getTrustMessage(),
                "AI",
                false,
                candidates,
                days,
                flatPlaces,
                route.getCreatedAt(),
                route.getUpdatedAt()
        );
    }

    private String join(List<String> values) {
        if (values == null || values.isEmpty()) return null;
        return String.join(",", values.stream().filter(value -> value != null && !value.isBlank()).toList());
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) return List.of();
        return List.of(value.split(","));
    }

    private String styleForStorage(AiRouteDto request) {
        LinkedHashSet<String> styles = new LinkedHashSet<>();
        if (request.travelStyles() != null) {
            request.travelStyles().stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(String::trim)
                    .forEach(styles::add);
        }
        if (request.style() != null && !request.style().isBlank()) {
            for (String value : request.style().split("[,·]")) {
                if (!value.isBlank()) styles.add(value.trim());
            }
        }
        return styles.isEmpty() ? request.style() : String.join(",", styles);
    }

    private String regionForStorage(AiRouteDto request) {
        LinkedHashSet<String> regions = new LinkedHashSet<>();
        if (request.regions() != null) {
            request.regions().stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(String::trim)
                    .forEach(regions::add);
        }
        if (request.region() != null && !request.region().isBlank()) {
            for (String value : request.region().split("[,·]")) {
                if (!value.isBlank()) regions.add(value.trim());
            }
        }
        return regions.isEmpty() ? request.region() : String.join(",", regions);
    }

    private List<String> splitStyles(String value) {
        if (value == null || value.isBlank()) return List.of();
        return List.of(value.split("[,·]")).stream()
                .map(String::trim)
                .filter(style -> !style.isBlank())
                .toList();
    }

    private String firstText(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        return fallback;
    }
}
