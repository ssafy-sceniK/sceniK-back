package com.scenik.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scenik.domain.CandidatePlace;
import com.scenik.dto.RouteDtos.AiRouteDto;
import com.scenik.dto.RouteDtos.AiRouteRequest;
import com.scenik.dto.RouteDtos.CandidatePlaceDto;
import com.scenik.dto.RouteDtos.RouteDayDto;
import com.scenik.dto.RouteDtos.RouteStopDto;
import com.scenik.mapper.AiRouteMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiRouteService {
    private static final Logger log = LoggerFactory.getLogger(AiRouteService.class);
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final AiRouteMapper aiRouteMapper;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String gmsBaseUrl;
    private final String gmsApiKey;
    private final String gmsModel;
    private final int defaultMinPlacesPerDay;
    private final int defaultPreferredPlacesPerDay;
    private final int defaultMaxPlacesPerDay;
    private final Map<String, GenerationState> generationCache = new ConcurrentHashMap<>();

    public AiRouteService(
            AiRouteMapper aiRouteMapper,
            ObjectMapper objectMapper,
            @Value("${scenik.ai.gms.base-url}") String gmsBaseUrl,
            @Value("${scenik.ai.gms.api-key:}") String gmsApiKey,
            @Value("${scenik.ai.gms.model:gpt-5.4-mini}") String gmsModel,
            @Value("${scenik.ai.gms.connect-timeout-ms:1000}") int connectTimeoutMs,
            @Value("${scenik.ai.gms.read-timeout-ms:5000}") int readTimeoutMs,
            @Value("${scenik.ai.route.min-places-per-day:2}") int defaultMinPlacesPerDay,
            @Value("${scenik.ai.route.preferred-places-per-day:3}") int defaultPreferredPlacesPerDay,
            @Value("${scenik.ai.route.max-places-per-day:6}") int defaultMaxPlacesPerDay
    ) {
        this.aiRouteMapper = aiRouteMapper;
        this.objectMapper = objectMapper;
        this.gmsBaseUrl = normalizeBaseUrl(gmsBaseUrl);
        this.gmsApiKey = gmsApiKey == null ? "" : gmsApiKey.trim();
        this.gmsModel = gmsModel;
        this.defaultMinPlacesPerDay = Math.max(1, defaultMinPlacesPerDay);
        this.defaultPreferredPlacesPerDay = Math.max(this.defaultMinPlacesPerDay, defaultPreferredPlacesPerDay);
        this.defaultMaxPlacesPerDay = Math.max(this.defaultPreferredPlacesPerDay, defaultMaxPlacesPerDay);
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public AiRouteDto generateRoute(AiRouteRequest request) {
        AiRouteRequest normalizedRequest = request == null ? new AiRouteRequest(null, List.of(), null, List.of(), 1, null, null, null, null, List.of(), List.of(), null, null, List.of(), null, null, null, null) : request;
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
        if (contentIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected content is required.");
        }
        int requestedDays = Math.max(1, request.durationDays() == null ? 1 : request.durationDays());
        StopPolicy stopPolicy = stopPolicy(request, requestedDays);
        int targetCount = Math.max(36, requestedDays * stopPolicy.maxPlacesPerDay() * 3);
        List<String> regions = normalizeRegions(request);
        String regionLabel = regionLabel(regions);
        List<CandidatePlace> selectedContentCandidates = deduplicate(aiRouteMapper.findCandidatePlaces(contentIds, null, targetCount));
        logSelectedContentCandidateCounts(contentIds, regions, selectedContentCandidates);
        List<CandidatePlace> candidates = filterByRegions(selectedContentCandidates, regions);
        int regionFilteredCount = candidates.size();
        int targetStopCount = targetStopCount(stopPolicy, requestedDays);
        int minimumStopCount = minimumStopCount(stopPolicy, requestedDays);
        candidates = supplementCandidatePlaces(candidates, selectedContentCandidates, regions, targetStopCount, targetCount);
        log.info(
                "AI route candidate filtering contentIds={} selectedRegions={} contentPlaceCount={} regionFilteredCount={} finalPromptCount={} stopPolicy={}",
                contentIds,
                regions,
                selectedContentCandidates.size(),
                regionFilteredCount,
                candidates.size(),
                stopPolicy
        );
        if (candidates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "선택한 콘텐츠와 지역에 해당하는 장소가 부족합니다.");
        }
        candidates = sortCandidatesForItinerary(candidates);
        if (offset > 0 && candidates.size() > 1) {
            Collections.rotate(candidates, -(offset % candidates.size()));
        }

        int dayCount = Math.min(requestedDays, candidates.size());
        if (dayCount < requestedDays) {
            log.info("AI route duration adjusted by strict region filter requestedDays={} generatedDays={} finalPromptCount={}", requestedDays, dayCount, candidates.size());
        }
        String region = firstText(regionLabel, candidates.get(0).getRegion());
        String contentTitle = contentTitleLabel(contentIds, candidates);
        String style = styleLabel(request);
        List<CandidatePlace> routeCandidates = candidates;
        List<RouteDayDto> days = generateAiDays(request, routeCandidates, dayCount, stopPolicy)
                .orElseGet(() -> buildDeterministicDays(routeCandidates, dayCount, style, request.hotelAddress(), stopPolicy));
        days = regroupDailyRoutesByArea(days, stopPolicy, request);
        days = optimizeDailyRoutes(days, request);
        List<CandidatePlaceDto> candidateDtos = candidates.stream().map(CandidatePlaceDto::from).toList();
        List<RouteStopDto> flatPlaces = days.stream().flatMap(day -> day.places().stream()).toList();

        return new AiRouteDto(
                generationId,
                generationId,
                contentTitle + " " + region + " " + style + " 코스",
                buildSummary(request, contentTitle, region, dayCount),
                contentIds.isEmpty() ? null : contentIds.get(0),
                contentIds,
                contentTitle,
                region,
                regions,
                dayCount,
                requestedDays,
                request.startDate(),
                request.endDate(),
                request.dateRangeLabel(),
                request.transport(),
                request.transportModes(),
                request.carDays(),
                request.companion(),
                request.style(),
                normalizeTravelStyles(request),
                request.budgetMin(),
                request.budgetMax(),
                request.budget(),
                trustMessage(candidates.size(), minimumStopCount),
                "AI",
                false,
                candidateDtos,
                days,
                flatPlaces,
                null,
                null
        );
    }

    private Optional<List<RouteDayDto>> generateAiDays(AiRouteRequest request, List<CandidatePlace> candidates, int dayCount, StopPolicy stopPolicy) {
        if (gmsApiKey.isBlank()) {
            log.info("GMS API key is not configured. Falling back to deterministic AI route generation.");
            return Optional.empty();
        }
        try {
            GmsChatResponse response = restClient.post()
                    .uri(URI.create(gmsBaseUrl + "api.openai.com/v1/chat/completions"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + gmsApiKey)
                    .body(buildGmsRequest(request, candidates, dayCount, stopPolicy))
                    .retrieve()
                    .body(GmsChatResponse.class);
            String content = response == null || response.choices() == null || response.choices().isEmpty()
                    ? null
                    : response.choices().get(0).content();
            AiRoutePlan plan = parseAiRoutePlan(content);
            return validateAndBuildAiDays(plan, candidates, dayCount, stopPolicy);
        } catch (RestClientException | IllegalArgumentException | JsonProcessingException exception) {
            log.warn("GMS AI route generation failed. Falling back to deterministic route. reason={}", exception.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    private GmsChatRequest buildGmsRequest(AiRouteRequest request, List<CandidatePlace> candidates, int dayCount, StopPolicy stopPolicy) throws JsonProcessingException {
        int minPlacesPerDay = minPlacesPerDay(candidates, dayCount, stopPolicy);
        int preferredPlacesPerDay = preferredPlacesPerDay(candidates, dayCount, stopPolicy);
        Map<String, Object> routeContext = new LinkedHashMap<>();
        routeContext.put("contentIds", normalizeContentIds(request));
        routeContext.put("region", regionLabel(normalizeRegions(request)));
        routeContext.put("regions", normalizeRegions(request));
        routeContext.put("durationDays", dayCount);
        routeContext.put("minPlacesPerDay", minPlacesPerDay);
        routeContext.put("preferredPlacesPerDay", preferredPlacesPerDay);
        routeContext.put("maxPlacesPerDay", stopPolicy.maxPlacesPerDay());
        routeContext.put("targetStopCount", Math.min(candidates.size(), targetStopCount(stopPolicy, dayCount)));
        routeContext.put("dailyStopPolicy", stopPolicy);
        routeContext.put("startDate", request.startDate());
        routeContext.put("endDate", request.endDate());
        routeContext.put("dateRangeLabel", request.dateRangeLabel());
        routeContext.put("transport", request.transport());
        routeContext.put("transportModes", request.transportModes());
        routeContext.put("carDays", request.carDays());
        routeContext.put("companion", request.companion());
        routeContext.put("style", request.style());
        routeContext.put("travelStyles", normalizeTravelStyles(request));
        routeContext.put("budgetMin", request.budgetMin());
        routeContext.put("budgetMax", request.budgetMax());
        routeContext.put("budget", request.budget());
        routeContext.put("hotelAddress", request.hotelAddress());
        routeContext.put("candidatePlaces", candidates.stream().map(this::promptCandidate).toList());
        routeContext.put("areaClusters", areaClusterSummary(candidates));
        routeContext.put("visitWorthyCategories", List.of("촬영지", "관광지", "궁궐", "전통마을", "거리", "카페", "공원", "문화시설"));
        routeContext.put("lowerPriorityCategories", List.of("학교", "호텔", "일반 상점", "업무시설"));

        String userPrompt = """
                아래 JSON 컨텍스트만 근거로 한국어 여행 코스를 생성하세요.
                규칙:
                - 응답은 한국어로 작성하세요.
                - DB에서 조회한 candidatePlaces 데이터만 사용하세요.
                - 장소명과 주소는 candidatePlaces에 제공된 값을 그대로 사용하세요.
                - 없는 장소, 없는 주소, 임의 촬영지, 임의 식당, 임의 카페를 만들지 마세요.
                - 사용자가 선택한 여행 지역은 routeContext.regions 입니다.
                - 반드시 routeContext.regions 중 하나에 속한 장소만 사용하세요.
                - candidatePlaces에는 선택 콘텐츠와 같은 지역의 검증 보강 장소가 함께 포함될 수 있습니다. candidatePlaces 밖의 장소를 절대 사용하지 마세요.
                - candidatePlaces에 있더라도 routeContext.regions 밖의 장소, 주소, 동선을 임의로 추가하지 마세요.
                - 선택하지 않은 지역의 장소는 사용하지 마세요.
                - 선택한 travelStyles 중 하나라도 만족하는 장소를 우선 배치하고, 부족하면 같은 지역 후보 중 동선이 자연스러운 장소를 사용하세요.
                - candidatePlaces의 areaCluster와 areaClusterLabel을 참고해 같은 생활권 장소를 같은 day에 우선 묶으세요.
                - 서로 다른 생활권을 한 day에 섞지 마세요. 예: 종로/중구, 성동/성수, 강남/청담/압구정, 송파/잠실, 마포/홍대/연남, 영등포/여의도는 각각 한 day 안에서 묶는 것이 좋습니다.
                - 정확히 durationDays 개의 day를 생성하세요. 2박 3일이면 반드시 3일차까지 생성하세요.
                - 각 day는 최소 minPlacesPerDay개 이상의 장소를 포함하세요.
                - 가능한 한 preferredPlacesPerDay에 가깝게 장소를 배치하세요.
                - 하루 장소 수는 maxPlacesPerDay를 넘기지 말고, 이동수단·동행유형·예산 조건상 무리한 과밀 일정을 만들지 마세요.
                - 첫날과 마지막날은 체크인·체크아웃·역/공항 이동을 고려해 중간 날짜보다 짧게 구성하세요.
                - 매일 같은 시간표를 반복하지 말고 오전, 점심 전후, 오후, 저녁 흐름이 자연스럽게 보이도록 시간을 다르게 배치하세요.
                - visitWorthyCategories에 가까운 장소를 우선하고, 학교/호텔/일반 상점은 콘텐츠 연결성이 강하거나 대체 후보가 없을 때만 사용하세요.
                - recommendReason은 장소별로 한 문장만 작성하고, contentId나 DB 검증 정책을 쓰지 마세요.
                - travelTip에는 운영 시간 확인 같은 공통 경고를 반복하지 말고, 장소 주변 동선이나 휴식 포인트가 있을 때만 짧게 작성하세요.
                - 장소 간 이동 흐름이 자연스럽도록 가까운 지역끼리 묶으세요.
                - hotelAddress가 비어 있지 않으면 전체 동선 판단에만 참고하고, 모든 장소 설명에 숙소 문구를 반복하지 마세요.
                - budget, region, style, travelStyles, transport, transportModes, carDays, companion 조건을 추천 이유와 이동 팁에 반영하세요.
                - carDays에 포함된 Day는 차량 이동을 우선 고려하고, 포함되지 않은 Day는 대중교통 이동을 기준으로 동선을 구성하세요.
                - 백엔드가 좌표 기반으로 최종 방문 순서를 재정렬하므로, 먼저 조건에 가장 맞는 placeId를 고르는 데 집중하세요.
                - 같은 placeId를 두 번 쓰지 마세요.
                - 출력은 설명 없이 JSON 객체만 반환하세요.
                - JSON 형식: {"summary":"...","days":[{"dayNumber":1,"estimatedTravelTime":"이동 약 60분","places":[{"placeId":"P_001","visitOrder":1,"recommendedTime":"10:00","recommendReason":"...","estimatedStayMinutes":60,"estimatedTravelMinutes":0,"travelTip":"..."}]}]}

                컨텍스트:
                %s
                """.formatted(objectMapper.writeValueAsString(routeContext));

        return new GmsChatRequest(
                gmsModel,
                List.of(
                        new GmsMessage("developer", "Answer in Korean. Return strict JSON only. Use only provided DB candidate placeIds."),
                        new GmsMessage("user", userPrompt)
                ),
                Map.of("type", "json_object")
        );
    }

    private Map<String, Object> promptCandidate(CandidatePlace place) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("placeId", place.getPlaceId());
        item.put("name", place.getName());
        item.put("region", place.getRegion());
        item.put("category", place.getCategory());
        item.put("address", place.getAddress());
        item.put("latitude", place.getLatitude());
        item.put("longitude", place.getLongitude());
        item.put("relatedContentId", place.getRelatedContentId());
        item.put("relatedContentTitle", place.getRelatedContentTitle());
        item.put("sceneDescription", place.getSceneDescription());
        item.put("verified", Boolean.TRUE.equals(place.getVerified()));
        AreaCluster areaCluster = areaCluster(place);
        item.put("areaCluster", areaCluster.id());
        item.put("areaClusterLabel", areaCluster.label());
        return item;
    }

    private AiRoutePlan parseAiRoutePlan(String content) throws JsonProcessingException {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("AI response content is empty.");
        }
        return objectMapper.readValue(extractJsonObject(content), AiRoutePlan.class);
    }

    private String extractJsonObject(String content) {
        String trimmed = content.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalArgumentException("AI response did not include a JSON object.");
        }
        return trimmed.substring(start, end + 1);
    }

    private Optional<List<RouteDayDto>> validateAndBuildAiDays(AiRoutePlan plan, List<CandidatePlace> candidates, int dayCount, StopPolicy stopPolicy) {
        if (plan == null || plan.days() == null || plan.days().size() != dayCount) {
            return Optional.empty();
        }
        int minPlacesPerDay = minPlacesPerDay(candidates, dayCount, stopPolicy);
        Map<String, CandidatePlace> candidateMap = candidates.stream()
                .collect(Collectors.toMap(CandidatePlace::getPlaceId, place -> place, (left, right) -> left, LinkedHashMap::new));
        Set<String> usedPlaceIds = new LinkedHashSet<>();
        List<RouteDayDto> days = new ArrayList<>();
        int removedPlaces = 0;

        for (int dayIndex = 0; dayIndex < dayCount; dayIndex++) {
            AiRoutePlanDay planDay = plan.days().get(dayIndex);
            if (planDay == null || planDay.dayNumber() == null || planDay.dayNumber() != dayIndex + 1 || planDay.places() == null) {
                return Optional.empty();
            }
            List<RouteStopDto> stops = new ArrayList<>();
            for (AiRoutePlanStop planStop : planDay.places()) {
                if (planStop == null || planStop.placeId() == null || !candidateMap.containsKey(planStop.placeId()) || !usedPlaceIds.add(planStop.placeId())) {
                    removedPlaces++;
                    continue;
                }
                CandidatePlace place = candidateMap.get(planStop.placeId());
                int visitOrder = stops.size() + 1;
                stops.add(new RouteStopDto(
                        null,
                        place.getPlaceId(),
                        place.getName(),
                        place.getName(),
                        dayIndex + 1,
                        visitOrder,
                        place.getAddress(),
                        place.getLatitude(),
                        place.getLongitude(),
                        place.getImageUrl(),
                        placeCategory(place),
                        normalizedRecommendedTime(planStop.recommendedTime(), dayIndex, visitOrder - 1),
                        placeCategory(place),
                        placeCategory(place),
                        positiveOrDefault(planStop.estimatedStayMinutes(), 60),
                        Math.max(0, planStop.estimatedTravelMinutes() == null ? (visitOrder == 1 ? 0 : 25) : planStop.estimatedTravelMinutes()),
                        usefulTravelTip(planStop.travelTip(), place, dayIndex, visitOrder - 1, planDay.places().size())
                ));
            }
            if (stops.size() > stopPolicy.maxPlacesPerDay()) {
                log.info(
                        "AI route response trimmed to maxPlacesPerDay day={} originalStopCount={} maxPlacesPerDay={}",
                        dayIndex + 1,
                        stops.size(),
                        stopPolicy.maxPlacesPerDay()
                );
                stops = new ArrayList<>(stops.subList(0, stopPolicy.maxPlacesPerDay()));
            }
            if (stops.size() < minPlacesPerDay) {
                log.info("AI route response rejected by regional validation removedPlaceCount={}", removedPlaces);
                return Optional.empty();
            }
            days.add(new RouteDayDto(dayIndex + 1, firstText(planDay.estimatedTravelTime(), estimateTravelTime(stops)), stops));
        }

        log.info("AI route response regional validation removedPlaceCount={}", removedPlaces);
        return Optional.of(days);
    }

    private List<RouteDayDto> buildDeterministicDays(List<CandidatePlace> candidates, int dayCount, String style, String hotelAddress, StopPolicy stopPolicy) {
        List<List<CandidatePlace>> groups = new ArrayList<>();
        for (int i = 0; i < dayCount; i++) {
            groups.add(new ArrayList<>());
        }
        int maxStopCount = Math.min(candidates.size(), Math.max(dayCount, targetStopCount(stopPolicy, dayCount)));
        List<CandidatePlace> selectedPlaces = sortCandidatesForItinerary(candidates).stream()
                .limit(maxStopCount)
                .sorted(routeOrderComparator())
                .toList();
        int placesPerDay = Math.max(1, (int) Math.ceil(selectedPlaces.size() / (double) dayCount));
        for (int index = 0; index < selectedPlaces.size(); index++) {
            groups.get(Math.min(dayCount - 1, index / placesPerDay)).add(selectedPlaces.get(index));
        }

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
                        placeCategory(place),
                        recommendedTime(dayIndex, placeIndex),
                        placeReason(place, style),
                        placeReason(place, style),
                        60,
                        placeIndex == 0 ? 0 : 25,
                        usefulTravelTip(null, place, dayIndex, placeIndex, places.size())
                ));
            }
            days.add(new RouteDayDto(dayNumber, estimateTravelTime(stops), stops));
        }
        return days;
    }

    private List<RouteDayDto> regroupDailyRoutesByArea(List<RouteDayDto> days, StopPolicy stopPolicy, AiRouteRequest request) {
        if (days == null || days.size() < 2) {
            return days;
        }
        List<RouteStopDto> stops = days.stream()
                .flatMap(day -> day.places() == null ? List.<RouteStopDto>of().stream() : day.places().stream())
                .toList();
        if (stops.isEmpty()) {
            return days;
        }

        List<AreaStopGroup> areaGroups = areaStopGroups(stops).stream()
                .sorted(Comparator.comparingInt((AreaStopGroup group) -> group.stops().size()).reversed())
                .toList();
        List<List<AreaStopGroup>> groupedAreas = areaGroups.size() <= days.size()
                ? assignAreaGroupsToTargetDays(areaGroups, stops.size(), days.size(), stopPolicy)
                : clusterAreaGroupsForRouteQuality(areaGroups, days.size(), stopPolicy, request);

        List<RouteDayDto> regrouped = new ArrayList<>();
        for (int index = 0; index < days.size(); index++) {
            List<RouteStopDto> dayStops = index >= groupedAreas.size()
                    ? List.of()
                    : groupedAreas.get(index).stream()
                            .flatMap(group -> optimizeSameAreaStops(group.stops()).stream())
                            .toList();
            regrouped.add(new RouteDayDto(index + 1, estimateTravelTime(dayStops), rebuildStopsAfterAreaGrouping(dayStops, index)));
        }
        logAreaGroupingSummary(regrouped);
        return regrouped;
    }

    private List<List<AreaStopGroup>> assignAreaGroupsToTargetDays(List<AreaStopGroup> areaGroups, int totalStopCount, int dayCount, StopPolicy stopPolicy) {
        int[] dayTargets = dayStopTargets(totalStopCount, dayCount, stopPolicy);
        List<List<AreaStopGroup>> groupedAreas = new ArrayList<>();
        List<List<RouteStopDto>> groupedStops = new ArrayList<>();
        for (int index = 0; index < dayCount; index++) {
            groupedAreas.add(new ArrayList<>());
            groupedStops.add(new ArrayList<>());
        }

        for (AreaStopGroup areaGroup : areaGroups) {
            List<RouteStopDto> areaStops = optimizeSameAreaStops(areaGroup.stops());
            int offset = 0;
            while (offset < areaStops.size()) {
                int remaining = areaStops.size() - offset;
                int dayIndex = bestDayForArea(areaGroup.areaId(), remaining, groupedStops, dayTargets);
                List<RouteStopDto> dayStops = groupedStops.get(dayIndex);
                int available = Math.max(1, dayTargets[Math.min(dayIndex, dayTargets.length - 1)] - dayStops.size());
                int take = Math.min(remaining, available);
                AreaStopGroup slice = new AreaStopGroup(areaGroup.areaId(), areaGroup.areaLabel(), areaStops.subList(offset, offset + take));
                groupedAreas.get(dayIndex).add(slice);
                dayStops.addAll(slice.stops());
                offset += take;
            }
        }
        return groupedAreas;
    }

    private List<List<AreaStopGroup>> clusterAreaGroupsForRouteQuality(List<AreaStopGroup> areaGroups, int dayCount, StopPolicy stopPolicy, AiRouteRequest request) {
        TransportProfile profile = conservativeTransportProfile(request);
        List<DayAreaBucket> buckets = areaGroups.stream()
                .map(group -> new DayAreaBucket(new ArrayList<>(List.of(group))))
                .collect(Collectors.toCollection(ArrayList::new));

        while (buckets.size() > dayCount) {
            AreaBucketMerge merge = bestAcceptableAreaBucketMerge(buckets, stopPolicy, profile);
            if (merge == null) {
                int removedIndex = lowestValueBucketIndex(buckets);
                DayAreaBucket removed = buckets.remove(removedIndex);
                log.info(
                        "AI route area bucket removed for day quality areaLabels={} stopCount={}",
                        removed.groups().stream().map(AreaStopGroup::areaLabel).toList(),
                        bucketStops(removed).size()
                );
                continue;
            }
            DayAreaBucket merged = mergeBuckets(buckets.get(merge.leftIndex()), buckets.get(merge.rightIndex()));
            buckets.remove(merge.rightIndex());
            buckets.remove(merge.leftIndex());
            buckets.add(merged);
        }

        List<DayAreaBucket> ordered = orderDayBucketsForTravel(buckets);
        List<List<AreaStopGroup>> groupedAreas = ordered.stream()
                .map(DayAreaBucket::groups)
                .collect(Collectors.toCollection(ArrayList::new));
        while (groupedAreas.size() < dayCount) {
            groupedAreas.add(new ArrayList<>());
        }
        return groupedAreas;
    }

    private AreaBucketMerge bestAcceptableAreaBucketMerge(List<DayAreaBucket> buckets, StopPolicy stopPolicy, TransportProfile profile) {
        AreaBucketMerge best = null;
        for (int left = 0; left < buckets.size(); left++) {
            for (int right = left + 1; right < buckets.size(); right++) {
                DayAreaBucket merged = mergeBuckets(buckets.get(left), buckets.get(right));
                if (!isAreaBucketAcceptable(merged, stopPolicy, profile)) {
                    continue;
                }
                double score = distanceKm(centroidStop(buckets.get(left)), centroidStop(buckets.get(right)))
                        + totalDistanceKm(optimizeStopOrder(bucketStops(merged))) * 0.15
                        + Math.max(0, bucketStops(merged).size() - stopPolicy.preferredPlacesPerDay()) * 8.0;
                if (best == null || score < best.score()) {
                    best = new AreaBucketMerge(left, right, score);
                }
            }
        }
        return best;
    }

    private boolean isAreaBucketAcceptable(DayAreaBucket bucket, StopPolicy stopPolicy, TransportProfile profile) {
        List<RouteStopDto> stops = optimizeStopOrder(bucketStops(bucket));
        if (stops.size() > stopPolicy.maxPlacesPerDay()) {
            return false;
        }
        if (bucket.groups().size() > 3) {
            return false;
        }
        return maxSegmentTravelMinutes(stops, profile) <= maxSingleSegmentMinutes(profile)
                && totalTravelMinutes(stops, profile) <= maxDailyTravelMinutes(profile);
    }

    private int lowestValueBucketIndex(List<DayAreaBucket> buckets) {
        int selected = 0;
        for (int index = 1; index < buckets.size(); index++) {
            DayAreaBucket current = buckets.get(index);
            DayAreaBucket best = buckets.get(selected);
            int currentStops = bucketStops(current).size();
            int bestStops = bucketStops(best).size();
            if (currentStops < bestStops || (currentStops == bestStops && nearestBucketDistance(current, buckets) > nearestBucketDistance(best, buckets))) {
                selected = index;
            }
        }
        return selected;
    }

    private double nearestBucketDistance(DayAreaBucket bucket, List<DayAreaBucket> buckets) {
        return buckets.stream()
                .filter(other -> other != bucket)
                .mapToDouble(other -> distanceKm(centroidStop(bucket), centroidStop(other)))
                .min()
                .orElse(Double.MAX_VALUE);
    }

    private DayAreaBucket mergeBuckets(DayAreaBucket left, DayAreaBucket right) {
        List<AreaStopGroup> groups = new ArrayList<>(left.groups());
        groups.addAll(right.groups());
        return new DayAreaBucket(groups);
    }

    private List<DayAreaBucket> orderDayBucketsForTravel(List<DayAreaBucket> buckets) {
        return buckets.stream()
                .sorted(Comparator.comparingDouble((DayAreaBucket bucket) -> centroidStop(bucket).latitude())
                        .thenComparingDouble(bucket -> centroidStop(bucket).longitude()))
                .toList();
    }

    private List<RouteStopDto> bucketStops(DayAreaBucket bucket) {
        return bucket.groups().stream()
                .flatMap(group -> group.stops().stream())
                .toList();
    }

    private RouteStopDto centroidStop(DayAreaBucket bucket) {
        return centroidStop(bucketStops(bucket));
    }

    private RouteStopDto centroidStop(List<RouteStopDto> stops) {
        if (stops == null || stops.isEmpty()) {
            return null;
        }
        double latitude = stops.stream()
                .filter(stop -> stop.latitude() != null)
                .mapToDouble(RouteStopDto::latitude)
                .average()
                .orElse(stops.get(0).latitude() == null ? 0.0 : stops.get(0).latitude());
        double longitude = stops.stream()
                .filter(stop -> stop.longitude() != null)
                .mapToDouble(RouteStopDto::longitude)
                .average()
                .orElse(stops.get(0).longitude() == null ? 0.0 : stops.get(0).longitude());
        RouteStopDto first = stops.get(0);
        return new RouteStopDto(
                null,
                first.placeId(),
                first.name(),
                first.placeName(),
                first.dayNumber(),
                first.visitOrder(),
                first.address(),
                latitude,
                longitude,
                first.thumbnailUrl(),
                first.category(),
                first.recommendedTime(),
                first.recommendReason(),
                first.reason(),
                first.estimatedStayMinutes(),
                first.estimatedTravelMinutes(),
                first.travelTip()
        );
    }
    private int bestDayForArea(String areaId, int remainingAreaStopCount, List<List<RouteStopDto>> groupedDays, int[] dayTargets) {
        int bestEmptyFit = -1;
        int bestEmptyFitTarget = Integer.MAX_VALUE;
        int bestEmpty = -1;
        int bestEmptyTarget = -1;
        int sameAreaDay = -1;
        int sameAreaCapacity = -1;
        int fallbackDay = 0;
        int fallbackCapacity = Integer.MIN_VALUE;

        for (int index = 0; index < groupedDays.size(); index++) {
            List<RouteStopDto> dayStops = groupedDays.get(index);
            int target = dayTargets[Math.min(index, dayTargets.length - 1)];
            int capacity = target - dayStops.size();
            if (dayStops.isEmpty()) {
                if (target >= remainingAreaStopCount && target < bestEmptyFitTarget) {
                    bestEmptyFit = index;
                    bestEmptyFitTarget = target;
                }
                if (target > bestEmptyTarget) {
                    bestEmpty = index;
                    bestEmptyTarget = target;
                }
            } else if (dayStops.stream().allMatch(stop -> areaCluster(stop).id().equals(areaId)) && capacity > sameAreaCapacity) {
                sameAreaDay = index;
                sameAreaCapacity = capacity;
            }
            if (capacity > fallbackCapacity) {
                fallbackDay = index;
                fallbackCapacity = capacity;
            }
        }
        if (bestEmptyFit >= 0) return bestEmptyFit;
        if (bestEmpty >= 0) return bestEmpty;
        if (sameAreaDay >= 0) return sameAreaDay;
        return fallbackDay;
    }

    private List<AreaStopGroup> areaStopGroups(List<RouteStopDto> stops) {
        LinkedHashMap<String, List<RouteStopDto>> byArea = new LinkedHashMap<>();
        for (RouteStopDto stop : stops) {
            byArea.computeIfAbsent(areaCluster(stop).id(), key -> new ArrayList<>()).add(stop);
        }
        return byArea.entrySet().stream()
                .map(entry -> new AreaStopGroup(entry.getKey(), areaCluster(entry.getValue().get(0)).label(), entry.getValue()))
                .toList();
    }

    private List<RouteStopDto> rebuildStopsAfterAreaGrouping(List<RouteStopDto> stops, int dayIndex) {
        List<RouteStopDto> rebuilt = new ArrayList<>();
        for (int index = 0; index < stops.size(); index++) {
            RouteStopDto stop = stops.get(index);
            rebuilt.add(new RouteStopDto(
                    stop.routePlaceId(),
                    stop.placeId(),
                    stop.name(),
                    stop.placeName(),
                    dayIndex + 1,
                    index + 1,
                    stop.address(),
                    stop.latitude(),
                    stop.longitude(),
                    stop.thumbnailUrl(),
                    stop.category(),
                    stop.recommendedTime(),
                    stop.recommendReason(),
                    stop.reason(),
                    stop.estimatedStayMinutes(),
                    stop.estimatedTravelMinutes(),
                    stop.travelTip()
            ));
        }
        return rebuilt;
    }

    private int[] dayStopTargets(int totalStopCount, int dayCount, StopPolicy stopPolicy) {
        int[] targets = new int[dayCount];
        if (dayCount <= 0) {
            return targets;
        }
        if (dayCount == 1) {
            targets[0] = Math.min(totalStopCount, stopPolicy.maxPlacesPerDay());
            return targets;
        }
        if (dayCount == 2 || totalStopCount < 11) {
            int base = totalStopCount / dayCount;
            int remainder = totalStopCount % dayCount;
            for (int index = 0; index < dayCount; index++) {
                targets[index] = base + (index < remainder ? 1 : 0);
            }
            return targets;
        }

        int edgeCap = Math.min(4, stopPolicy.maxPlacesPerDay());
        int middleCap = stopPolicy.maxPlacesPerDay();
        int remaining = totalStopCount;
        targets[0] = Math.min(edgeCap, Math.max(3, remaining / dayCount));
        remaining -= targets[0];
        targets[dayCount - 1] = Math.min(edgeCap, Math.max(3, remaining / dayCount));
        remaining -= targets[dayCount - 1];
        int middleDays = dayCount - 2;
        for (int index = 1; index < dayCount - 1; index++) {
            int daysLeft = dayCount - 1 - index;
            int target = daysLeft <= 0 ? remaining : (int) Math.ceil(remaining / (double) daysLeft);
            targets[index] = Math.min(middleCap, Math.max(stopPolicy.preferredPlacesPerDay(), target));
            remaining -= targets[index];
        }
        int index = 1;
        while (remaining > 0 && dayCount > 2) {
            if (targets[index] < middleCap) {
                targets[index]++;
                remaining--;
            }
            index++;
            if (index >= dayCount - 1) {
                index = 1;
                if (targets[index] >= middleCap && remaining > 0) {
                    break;
                }
            }
        }
        if (remaining > 0) {
            targets[dayCount - 1] += remaining;
        }
        return targets;
    }

    private List<RouteDayDto> optimizeDailyRoutes(List<RouteDayDto> days, AiRouteRequest request) {
        if (days == null || days.isEmpty()) {
            return days;
        }
        int mixedDayCount = 0;
        List<RouteDayDto> optimizedDays = new ArrayList<>();
        for (int dayIndex = 0; dayIndex < days.size(); dayIndex++) {
            RouteDayDto day = days.get(dayIndex);
            TransportProfile profile = transportProfileForDay(request, dayIndex + 1);
            List<RouteStopDto> originalStops = day.places() == null ? List.of() : day.places();
            List<RouteStopDto> optimizedStops = optimizeStopOrder(originalStops);
            double beforeKm = totalDistanceKm(originalStops);
            double afterKm = totalDistanceKm(optimizedStops);
            boolean reordered = !placeIdOrder(originalStops).equals(placeIdOrder(optimizedStops));
            List<RouteStopDto> rebuiltStops = rebuildStopsAfterOptimization(optimizedStops, dayIndex, profile);
            log.info(
                    "AI route distance optimization day={} transportProfile={} reordered={} beforeKm={} afterKm={} stopCount={}",
                    dayIndex + 1,
                    profile.label(),
                    reordered,
                    roundDistance(beforeKm),
                    roundDistance(afterKm),
                    rebuiltStops.size()
            );
            List<AreaCluster> dayAreas = dayAreaClusters(rebuiltStops);
            boolean mixedArea = dayAreas.size() > 1;
            if (mixedArea) {
                mixedDayCount++;
            }
            boolean edgeOverloaded = isEdgeDay(dayIndex, days.size()) && rebuiltStops.size() > 4;
            log.info(
                    "AI route area quality day={} areaClusters={} totalDistanceKm={} mixedArea={} stopCount={} edgeDayOverloaded={}",
                    dayIndex + 1,
                    dayAreas.stream().map(AreaCluster::label).toList(),
                    roundDistance(afterKm),
                    mixedArea,
                    rebuiltStops.size(),
                    edgeOverloaded
            );
            optimizedDays.add(new RouteDayDto(day.dayNumber(), estimateTravelTime(rebuiltStops), rebuiltStops));
        }
        log.info("AI route area quality summary mixedDayCount={} dayCount={}", mixedDayCount, days.size());
        return optimizedDays;
    }

    private List<RouteStopDto> optimizeStopOrder(List<RouteStopDto> stops) {
        if (stops.size() < 3 || stops.stream().anyMatch(stop -> !hasCoordinates(stop))) {
            return stops;
        }
        List<AreaStopGroup> areaGroups = areaStopGroups(stops);
        if (areaGroups.size() > 1) {
            return optimizeMixedAreaStopOrder(areaGroups);
        }
        return optimizeSameAreaStops(stops);
    }

    private List<RouteStopDto> optimizeMixedAreaStopOrder(List<AreaStopGroup> areaGroups) {
        List<AreaStopGroup> remaining = new ArrayList<>(areaGroups);
        List<AreaStopGroup> orderedGroups = new ArrayList<>();
        AreaStopGroup current = remaining.remove(0);
        orderedGroups.add(current);
        while (!remaining.isEmpty()) {
            AreaStopGroup currentGroup = current;
            AreaStopGroup next = remaining.stream()
                    .min(Comparator.comparingDouble(group -> distanceKm(currentGroup.centroidStop(), group.centroidStop())))
                    .orElseThrow();
            orderedGroups.add(next);
            remaining.remove(next);
            current = next;
        }
        return orderedGroups.stream()
                .flatMap(group -> optimizeSameAreaStops(group.stops()).stream())
                .toList();
    }

    private List<RouteStopDto> optimizeSameAreaStops(List<RouteStopDto> stops) {
        if (stops.size() < 3 || stops.stream().anyMatch(stop -> !hasCoordinates(stop))) {
            return stops;
        }
        List<RouteStopDto> best = stops;
        double bestDistance = totalDistanceKm(stops);
        for (RouteStopDto start : stops) {
            List<RouteStopDto> candidate = nearestNeighborFrom(stops, start);
            double distance = totalDistanceKm(candidate);
            if (distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    private List<RouteStopDto> nearestNeighborFrom(List<RouteStopDto> stops, RouteStopDto start) {
        List<RouteStopDto> remaining = new ArrayList<>(stops);
        List<RouteStopDto> ordered = new ArrayList<>();
        RouteStopDto current = start;
        ordered.add(current);
        remaining.remove(current);
        while (!remaining.isEmpty()) {
            RouteStopDto currentStop = current;
            RouteStopDto next = remaining.stream()
                    .min(Comparator.comparingDouble(stop -> distanceKm(currentStop, stop)))
                    .orElseThrow();
            ordered.add(next);
            remaining.remove(next);
            current = next;
        }
        return ordered;
    }

    private List<RouteStopDto> rebuildStopsAfterOptimization(List<RouteStopDto> stops, int dayIndex, TransportProfile profile) {
        List<RouteStopDto> rebuilt = new ArrayList<>();
        for (int index = 0; index < stops.size(); index++) {
            RouteStopDto stop = stops.get(index);
            int travelMinutes = index == 0 ? 0 : estimateTravelMinutes(distanceKm(stops.get(index - 1), stop), profile);
            rebuilt.add(new RouteStopDto(
                    stop.routePlaceId(),
                    stop.placeId(),
                    stop.name(),
                    stop.placeName(),
                    dayIndex + 1,
                    index + 1,
                    stop.address(),
                    stop.latitude(),
                    stop.longitude(),
                    stop.thumbnailUrl(),
                    stop.category(),
                    recommendedTime(dayIndex, index),
                    stop.recommendReason(),
                    stop.reason(),
                    stop.estimatedStayMinutes(),
                    travelMinutes,
                    stop.travelTip()
            ));
        }
        return rebuilt;
    }

    private int estimateTravelMinutes(double distanceKm, TransportProfile profile) {
        if (!Double.isFinite(distanceKm) || distanceKm <= 0) {
            return 0;
        }
        double minutes = distanceKm / profile.averageSpeedKph() * 60.0 + travelBufferMinutes(distanceKm, profile);
        return Math.max(5, (int) Math.round(minutes));
    }

    private int travelBufferMinutes(double distanceKm, TransportProfile profile) {
        if (profile == TransportProfile.WALK) return 0;
        if (profile == TransportProfile.CAR) return distanceKm >= 20.0 ? 10 : 5;
        return distanceKm >= 20.0 ? 15 : 8;
    }

    private int totalTravelMinutes(List<RouteStopDto> stops, TransportProfile profile) {
        int total = 0;
        for (int index = 1; index < stops.size(); index++) {
            total += estimateTravelMinutes(distanceKm(stops.get(index - 1), stops.get(index)), profile);
        }
        return total;
    }

    private int maxSegmentTravelMinutes(List<RouteStopDto> stops, TransportProfile profile) {
        int max = 0;
        for (int index = 1; index < stops.size(); index++) {
            max = Math.max(max, estimateTravelMinutes(distanceKm(stops.get(index - 1), stops.get(index)), profile));
        }
        return max;
    }

    private int maxSingleSegmentMinutes(TransportProfile profile) {
        return switch (profile) {
            case WALK -> 45;
            case TRANSIT -> 145;
            case CAR -> 135;
        };
    }

    private int maxDailyTravelMinutes(TransportProfile profile) {
        return switch (profile) {
            case WALK -> 90;
            case TRANSIT -> 240;
            case CAR -> 270;
        };
    }

    private double totalDistanceKm(List<RouteStopDto> stops) {
        if (stops == null || stops.size() < 2 || stops.stream().anyMatch(stop -> !hasCoordinates(stop))) {
            return 0.0;
        }
        double total = 0.0;
        for (int index = 1; index < stops.size(); index++) {
            total += distanceKm(stops.get(index - 1), stops.get(index));
        }
        return total;
    }

    private double distanceKm(RouteStopDto from, RouteStopDto to) {
        if (!hasCoordinates(from) || !hasCoordinates(to)) {
            return 0.0;
        }
        double lat1 = Math.toRadians(from.latitude());
        double lat2 = Math.toRadians(to.latitude());
        double deltaLat = Math.toRadians(to.latitude() - from.latitude());
        double deltaLng = Math.toRadians(to.longitude() - from.longitude());
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private boolean hasCoordinates(RouteStopDto stop) {
        return stop != null
                && stop.latitude() != null
                && stop.longitude() != null
                && Double.isFinite(stop.latitude())
                && Double.isFinite(stop.longitude());
    }

    private List<String> placeIdOrder(List<RouteStopDto> stops) {
        return stops.stream().map(RouteStopDto::placeId).toList();
    }

    private double roundDistance(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private void logAreaGroupingSummary(List<RouteDayDto> days) {
        for (RouteDayDto day : days) {
            List<RouteStopDto> stops = day.places() == null ? List.of() : day.places();
            List<AreaCluster> areas = dayAreaClusters(stops);
            log.info(
                    "AI route area grouping day={} areaClusters={} stopCount={} mixedArea={}",
                    day.dayNumber(),
                    areas.stream().map(AreaCluster::label).toList(),
                    stops.size(),
                    areas.size() > 1
            );
        }
    }

    private List<AreaCluster> dayAreaClusters(List<RouteStopDto> stops) {
        LinkedHashMap<String, AreaCluster> areas = new LinkedHashMap<>();
        for (RouteStopDto stop : stops) {
            AreaCluster area = areaCluster(stop);
            areas.putIfAbsent(area.id(), area);
        }
        return new ArrayList<>(areas.values());
    }

    private boolean isEdgeDay(int dayIndex, int dayCount) {
        return dayCount > 1 && (dayIndex == 0 || dayIndex == dayCount - 1);
    }

    private List<Map<String, Object>> areaClusterSummary(List<CandidatePlace> candidates) {
        Map<String, Long> counts = candidates.stream()
                .collect(Collectors.groupingBy(place -> areaCluster(place).id(), LinkedHashMap::new, Collectors.counting()));
        Map<String, AreaCluster> clusters = candidates.stream()
                .map(this::areaCluster)
                .collect(Collectors.toMap(AreaCluster::id, cluster -> cluster, (left, right) -> left, LinkedHashMap::new));
        List<Map<String, Object>> summary = new ArrayList<>();
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            AreaCluster cluster = clusters.get(entry.getKey());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("areaCluster", cluster.id());
            item.put("areaClusterLabel", cluster.label());
            item.put("candidateCount", entry.getValue());
            summary.add(item);
        }
        return summary;
    }

    private AreaCluster areaCluster(CandidatePlace place) {
        return areaCluster(firstText(place.getRegion(), ""), firstText(place.getAddress(), ""), firstText(place.getName(), ""));
    }

    private AreaCluster areaCluster(RouteStopDto stop) {
        return areaCluster("", firstText(stop.address(), ""), firstText(stop.placeName(), firstText(stop.name(), "")));
    }

    private AreaCluster areaCluster(String region, String address, String name) {
        String text = (firstText(region, "") + " " + firstText(address, "") + " " + firstText(name, "")).replaceAll("\\s+", "");
        if (containsAny(text, List.of("종로구", "중구", "광화문", "명동", "을지로", "인사동", "북촌", "서촌", "경복궁", "창덕궁", "덕수궁", "청계천"))) {
            return new AreaCluster("seoul-jongno-jung", "종로/중구");
        }
        if (containsAny(text, List.of("성동구", "성수", "뚝섬", "서울숲", "광진구", "건대", "자양", "화양"))) {
            return new AreaCluster("seoul-seongsu-geondae", "성동/성수/건대");
        }
        if (containsAny(text, List.of("강남구", "청담", "압구정", "삼성", "코엑스", "신사", "논현", "역삼", "도산대로", "가로수길"))) {
            return new AreaCluster("seoul-gangnam-cheongdam", "강남/청담/압구정");
        }
        if (containsAny(text, List.of("서초구", "양재", "반포", "서래", "교대", "고속터미널"))) {
            return new AreaCluster("seoul-seocho-yangjae", "서초/양재");
        }
        if (containsAny(text, List.of("송파구", "잠실", "석촌", "롯데월드", "올림픽공원", "방이"))) {
            return new AreaCluster("seoul-songpa-jamsil", "송파/잠실");
        }
        if (containsAny(text, List.of("마포구", "홍대", "연남", "합정", "상수", "망원", "공덕", "상암"))) {
            return new AreaCluster("seoul-mapo-hongdae", "마포/홍대/연남");
        }
        if (containsAny(text, List.of("영등포구", "여의도", "문래", "당산"))) {
            return new AreaCluster("seoul-yeongdeungpo-yeouido", "영등포/여의도");
        }
        if (containsAny(text, List.of("용산구", "이태원", "한남", "해방촌", "후암", "용리단길"))) {
            return new AreaCluster("seoul-yongsan", "용산/한남");
        }
        if (containsAny(text, List.of("서대문구", "신촌", "이대", "연희"))) {
            return new AreaCluster("seoul-seodaemun-sinchon", "서대문/신촌");
        }
        if (containsAny(text, List.of("강서구", "마곡", "김포공항"))) {
            return new AreaCluster("seoul-gangseo-magok", "강서/마곡");
        }

        String district = districtFromAddress(address);
        String normalizedRegion = normalizeRegion(region);
        if (!district.isBlank()) {
            String prefix = normalizedRegion.isBlank() ? "area" : normalizedRegion;
            return new AreaCluster(prefix + "-" + district, district);
        }
        String fallback = normalizedRegion.isBlank() ? "기타 권역" : normalizedRegion;
        return new AreaCluster("area-" + fallback, fallback);
    }

    private String districtFromAddress(String address) {
        if (address == null || address.isBlank()) {
            return "";
        }
        for (String part : address.split("\\s+")) {
            String asciiPart = part.toLowerCase(Locale.ROOT);
            if (part.endsWith("구") || part.endsWith("군") || part.endsWith("시")
                    || asciiPart.endsWith("-gu") || asciiPart.endsWith("-gun") || asciiPart.endsWith("-si")) {
                return part;
            }
        }
        return "";
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

    private List<CandidatePlace> filterByRegions(List<CandidatePlace> places, List<String> regions) {
        if (regions == null || regions.isEmpty()) {
            return places;
        }
        return places.stream()
                .filter(place -> regions.stream().anyMatch(region -> isInSelectedRegion(place, region)))
                .toList();
    }

    private void logSelectedContentCandidateCounts(List<String> contentIds, List<String> regions, List<CandidatePlace> candidates) {
        Map<String, Long> counts = candidates.stream()
                .collect(Collectors.groupingBy(CandidatePlace::getRelatedContentId, LinkedHashMap::new, Collectors.counting()));
        for (String contentId : contentIds) {
            long total = counts.getOrDefault(contentId, 0L);
            long regional = regions == null || regions.isEmpty() ? total : candidates.stream()
                    .filter(place -> contentId.equals(place.getRelatedContentId()) && regions.stream().anyMatch(region -> isInSelectedRegion(place, region)))
                    .count();
            log.info("AI route selected content places contentId={} regions={} total={} regional={}", contentId, regions, total, regional);
        }
    }

    private List<String> normalizeRegions(AiRouteRequest request) {
        LinkedHashSet<String> regions = new LinkedHashSet<>();
        if (request.regions() != null) {
            request.regions().stream()
                    .filter(region -> region != null && !region.isBlank())
                    .map(this::normalizeRegion)
                    .filter(region -> !region.isBlank())
                    .forEach(regions::add);
        }
        if (request.region() != null && !request.region().isBlank()) {
            for (String region : request.region().split("[,·]")) {
                String normalized = normalizeRegion(region);
                if (!normalized.isBlank()) regions.add(normalized);
            }
        }
        return new ArrayList<>(regions);
    }

    private String regionLabel(List<String> regions) {
        if (regions == null || regions.isEmpty()) {
            return "";
        }
        return String.join(",", regions);
    }

    private boolean isInSelectedRegion(CandidatePlace place, String selectedRegion) {
        if (place == null || selectedRegion == null || selectedRegion.isBlank()) {
            return true;
        }
        String selected = normalizeRegion(selectedRegion);
        return normalizeRegion(place.getRegion()).equals(selected)
                || normalizeRegion(place.getAddress()).equals(selected)
                || startsWithRegionAlias(place.getAddress(), selected);
    }

    private boolean startsWithRegionAlias(String text, String normalizedRegion) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalizedText = text.replaceAll("\\s+", "");
        return switch (normalizedRegion) {
            case "서울" -> normalizedText.startsWith("서울") || normalizedText.startsWith("서울특별시");
            case "부산" -> normalizedText.startsWith("부산") || normalizedText.startsWith("부산광역시");
            case "인천" -> normalizedText.startsWith("인천") || normalizedText.startsWith("인천광역시");
            case "대구" -> normalizedText.startsWith("대구") || normalizedText.startsWith("대구광역시");
            case "대전" -> normalizedText.startsWith("대전") || normalizedText.startsWith("대전광역시");
            case "광주" -> normalizedText.startsWith("광주") || normalizedText.startsWith("광주광역시");
            case "울산" -> normalizedText.startsWith("울산") || normalizedText.startsWith("울산광역시");
            case "세종" -> normalizedText.startsWith("세종") || normalizedText.startsWith("세종특별자치시");
            case "경기" -> normalizedText.startsWith("경기") || normalizedText.startsWith("경기도");
            case "강원" -> normalizedText.startsWith("강원") || normalizedText.startsWith("강원도") || normalizedText.startsWith("강원특별자치도");
            case "충북" -> normalizedText.startsWith("충북") || normalizedText.startsWith("충청북도");
            case "충남" -> normalizedText.startsWith("충남") || normalizedText.startsWith("충청남도");
            case "전북" -> normalizedText.startsWith("전북") || normalizedText.startsWith("전라북도") || normalizedText.startsWith("전북특별자치도");
            case "전남" -> normalizedText.startsWith("전남") || normalizedText.startsWith("전라남도");
            case "경북" -> normalizedText.startsWith("경북") || normalizedText.startsWith("경상북도");
            case "경남" -> normalizedText.startsWith("경남") || normalizedText.startsWith("경상남도");
            case "제주" -> normalizedText.startsWith("제주") || normalizedText.startsWith("제주특별자치도");
            default -> normalizedText.startsWith(normalizedRegion);
        };
    }

    private String normalizeRegion(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", "");
        if (normalized.startsWith("서울")) return "서울";
        if (normalized.startsWith("부산")) return "부산";
        if (normalized.startsWith("인천")) return "인천";
        if (normalized.startsWith("대구")) return "대구";
        if (normalized.startsWith("대전")) return "대전";
        if (normalized.startsWith("광주")) return "광주";
        if (normalized.startsWith("울산")) return "울산";
        if (normalized.startsWith("세종")) return "세종";
        if (normalized.startsWith("경기")) return "경기";
        if (normalized.startsWith("강원")) return "강원";
        if (normalized.startsWith("충북") || normalized.startsWith("충청북도")) return "충북";
        if (normalized.startsWith("충남") || normalized.startsWith("충청남도")) return "충남";
        if (normalized.startsWith("전북") || normalized.startsWith("전라북도")) return "전북";
        if (normalized.startsWith("전남") || normalized.startsWith("전라남도")) return "전남";
        if (normalized.startsWith("경북") || normalized.startsWith("경상북도")) return "경북";
        if (normalized.startsWith("경남") || normalized.startsWith("경상남도")) return "경남";
        if (normalized.startsWith("제주")) return "제주";
        return normalized;
    }

    private String contentTitleLabel(List<String> contentIds, List<CandidatePlace> candidates) {
        Map<String, String> titlesById = candidates.stream()
                .filter(place -> place.getRelatedContentId() != null && place.getRelatedContentTitle() != null)
                .collect(Collectors.toMap(CandidatePlace::getRelatedContentId, CandidatePlace::getRelatedContentTitle, (left, right) -> left, LinkedHashMap::new));
        List<String> titles = contentIds.stream()
                .map(contentId -> firstText(titlesById.get(contentId), "선택 콘텐츠"))
                .filter(title -> !looksLikeContentId(title))
                .distinct()
                .toList();
        if (titles.isEmpty()) {
            return firstText(candidates.get(0).getRelatedContentTitle(), "선택 콘텐츠");
        }
        return String.join(" · ", titles);
    }

    private boolean looksLikeContentId(String value) {
        return value != null && value.matches("C_[A-Z]+_\\d+");
    }

    private List<String> normalizeTravelStyles(AiRouteRequest request) {
        LinkedHashSet<String> styles = new LinkedHashSet<>();
        if (request.travelStyles() != null) {
            request.travelStyles().stream().filter(style -> style != null && !style.isBlank()).forEach(styles::add);
        }
        if (request.style() != null && !request.style().isBlank()) {
            for (String style : request.style().split("[,·]")) {
                if (!style.isBlank()) styles.add(style.trim());
            }
        }
        return new ArrayList<>(styles);
    }

    private String styleLabel(AiRouteRequest request) {
        List<String> styles = normalizeTravelStyles(request);
        return styles.isEmpty() ? "감성 여행" : String.join(" · ", styles);
    }

    private List<CandidatePlace> deduplicate(List<CandidatePlace> places) {
        LinkedHashMap<String, CandidatePlace> byId = new LinkedHashMap<>();
        for (CandidatePlace place : places) {
            byId.putIfAbsent(place.getPlaceId(), place);
        }
        return new ArrayList<>(byId.values());
    }

    private List<CandidatePlace> appendUnique(List<CandidatePlace> base, List<CandidatePlace> additional) {
        List<CandidatePlace> merged = new ArrayList<>(base == null ? List.of() : base);
        if (additional != null) merged.addAll(additional);
        return deduplicate(merged);
    }

    private List<CandidatePlace> sortCandidatesForItinerary(List<CandidatePlace> candidates) {
        return candidates.stream()
                .sorted(Comparator.comparingInt(this::placeVisitScore).reversed()
                        .thenComparing(routeOrderComparator()))
                .toList();
    }

    private Comparator<CandidatePlace> routeOrderComparator() {
        return Comparator
                .comparing((CandidatePlace place) -> firstText(place.getRegion(), ""))
                .thenComparingDouble(place -> coordinateOrZero(place.getLatitude()))
                .thenComparingDouble(place -> coordinateOrZero(place.getLongitude()))
                .thenComparing(place -> firstText(place.getRelatedContentTitle(), ""))
                .thenComparing(place -> firstText(place.getName(), ""));
    }

    private int placeVisitScore(CandidatePlace place) {
        String text = (firstText(place.getCategory(), "") + " " + firstText(place.getName(), "")).toLowerCase();
        int score = 50;
        if (containsAny(text, List.of("촬영", "관광", "명소", "궁", "전통", "마을", "거리", "카페", "공원", "문화", "박물관", "미술관", "시장", "해변", "숲"))) {
            score += 35;
        }
        if (containsAny(text, List.of("학교", "대학교", "고등학교", "호텔", "모텔", "숙소", "상점", "매장", "오피스", "회사", "병원", "아파트"))) {
            score -= 35;
        }
        if (place.getLatitude() != null && place.getLongitude() != null) score += 5;
        if (place.getSceneDescription() != null && !place.getSceneDescription().isBlank()) score += 5;
        return score;
    }

    private boolean containsAny(String text, List<String> needles) {
        return needles.stream().anyMatch(text::contains);
    }

    private double coordinateOrZero(Double value) {
        return value == null ? 0.0 : value;
    }

    private List<CandidatePlace> supplementCandidatePlaces(
            List<CandidatePlace> candidates,
            List<CandidatePlace> selectedContentCandidates,
            List<String> requestedRegions,
            int targetStopCount,
            int queryLimit
    ) {
        if (candidates.size() >= targetStopCount) {
            return candidates;
        }

        List<CandidatePlace> supplemented = new ArrayList<>(candidates);
        for (String region : supplementRegions(candidates, selectedContentCandidates, requestedRegions)) {
            if (supplemented.size() >= targetStopCount) break;
            supplemented = appendUnique(supplemented, aiRouteMapper.findCandidatePlaces(null, region, Math.max(queryLimit, targetStopCount * 4)));
        }

        supplemented = filterByRegions(supplemented, requestedRegions);
        if (supplemented.size() > candidates.size()) {
            log.info(
                    "AI route supplemented candidates selectedCount={} supplementedCount={} targetStopCount={} regions={}",
                    candidates.size(),
                    supplemented.size(),
                    targetStopCount,
                    requestedRegions
            );
        }
        return supplemented;
    }

    private List<String> supplementRegions(List<CandidatePlace> candidates, List<CandidatePlace> selectedContentCandidates, List<String> requestedRegions) {
        LinkedHashSet<String> regions = new LinkedHashSet<>();
        if (requestedRegions != null) {
            requestedRegions.stream()
                    .filter(region -> region != null && !region.isBlank())
                    .forEach(regions::add);
        }
        if (!regions.isEmpty()) {
            return new ArrayList<>(regions);
        }

        appendCandidateRegions(regions, candidates);
        appendCandidateRegions(regions, selectedContentCandidates);
        return new ArrayList<>(regions);
    }

    private void appendCandidateRegions(LinkedHashSet<String> regions, List<CandidatePlace> candidates) {
        if (candidates == null) return;
        candidates.stream()
                .map(CandidatePlace::getRegion)
                .filter(region -> region != null && !region.isBlank())
                .map(this::normalizeRegion)
                .filter(region -> !region.isBlank())
                .forEach(regions::add);
    }

    private String placeCategory(CandidatePlace place) {
        return firstText(place.getCategory(), "촬영지");
    }

    private int minPlacesPerDay(List<CandidatePlace> candidates, int dayCount, StopPolicy stopPolicy) {
        return candidates.size() >= dayCount * stopPolicy.minPlacesPerDay() ? stopPolicy.minPlacesPerDay() : 1;
    }

    private int preferredPlacesPerDay(List<CandidatePlace> candidates, int dayCount, StopPolicy stopPolicy) {
        return candidates.size() >= dayCount * stopPolicy.preferredPlacesPerDay()
                ? stopPolicy.preferredPlacesPerDay()
                : minPlacesPerDay(candidates, dayCount, stopPolicy);
    }

    private TransportProfile conservativeTransportProfile(AiRouteRequest request) {
        if (request == null) {
            return TransportProfile.TRANSIT;
        }
        String text = conditionText(request);
        if (containsAny(text, List.of("도보", "걷", "walk", "walking"))) {
            return TransportProfile.WALK;
        }
        boolean allDaysByCar = request.durationDays() != null
                && request.carDays() != null
                && request.durationDays() > 0
                && request.carDays().size() >= request.durationDays();
        boolean carOnlyMode = request.transportModes() != null
                && !request.transportModes().isEmpty()
                && request.transportModes().stream().allMatch(mode -> String.valueOf(mode).toLowerCase(Locale.ROOT).contains("car"));
        if ((allDaysByCar || carOnlyMode) && containsAny(text, List.of("차", "차량", "자동차", "car", "drive", "driving"))) {
            return TransportProfile.CAR;
        }
        return TransportProfile.TRANSIT;
    }

    private StopPolicy stopPolicy(AiRouteRequest request, int requestedDays) {
        int min = defaultMinPlacesPerDay;
        int preferred = defaultPreferredPlacesPerDay;
        int max = defaultMaxPlacesPerDay;
        TransportProfile profile = transportProfileForDay(request, 1);
        if (profile == TransportProfile.WALK) {
            preferred = Math.min(preferred, 3);
            max = Math.min(max, 4);
        } else if (profile == TransportProfile.CAR) {
            preferred = Math.max(preferred, 4);
            max = Math.max(max, 5);
        }
        String conditionText = conditionText(request);
        if (containsAny(conditionText, List.of("아이", "어린이", "유아", "부모", "시니어", "어르신", "힐링", "휴식", "relax", "healing"))) {
            preferred = Math.max(min, preferred - 1);
            max = Math.max(preferred, max - 1);
        }
        if (containsAny(conditionText, List.of("액티비티", "촬영", "덕질", "알차", "빡빡", "activity", "active"))) {
            preferred = Math.min(max, preferred + 1);
        }
        if (isLowBudget(request)) {
            preferred = Math.max(min, preferred - 1);
        }
        if (normalizeContentIds(request).size() > 1 && profile != TransportProfile.WALK) {
            preferred = Math.min(max, preferred + 1);
        }
        if (requestedDays <= 1 && profile != TransportProfile.WALK && !isLowBudget(request)) {
            preferred = Math.min(max, preferred + 1);
        }
        preferred = Math.max(min, Math.min(preferred, max));
        return new StopPolicy(min, preferred, max);
    }

    private int targetStopCount(StopPolicy stopPolicy, int requestedDays) {
        return Math.max(requestedDays, requestedDays * stopPolicy.preferredPlacesPerDay());
    }

    private int minimumStopCount(StopPolicy stopPolicy, int requestedDays) {
        return Math.max(requestedDays, requestedDays * stopPolicy.minPlacesPerDay());
    }

    private TransportProfile transportProfileForDay(AiRouteRequest request, int dayNumber) {
        if (request != null && request.carDays() != null && request.carDays().contains(dayNumber)) {
            return TransportProfile.CAR;
        }
        String text = conditionText(request);
        if (containsAny(text, List.of("도보", "걷", "walk", "walking"))) {
            return TransportProfile.WALK;
        }
        if (containsAny(text, List.of("차", "차량", "자동차", "렌터카", "렌트", "car", "drive", "driving"))) {
            return TransportProfile.CAR;
        }
        return TransportProfile.TRANSIT;
    }

    private String conditionText(AiRouteRequest request) {
        if (request == null) {
            return "";
        }
        return String.join(" ",
                firstText(request.transport(), ""),
                request.transportModes() == null ? "" : String.join(" ", request.transportModes()),
                firstText(request.companion(), ""),
                firstText(request.style(), ""),
                String.join(" ", normalizeTravelStyles(request)),
                firstText(request.budget(), "")
        ).toLowerCase();
    }

    private boolean isLowBudget(AiRouteRequest request) {
        if (request == null) {
            return false;
        }
        if (request.budgetMax() != null && request.durationDays() != null && request.durationDays() > 0) {
            return request.budgetMax() / request.durationDays() <= 80_000;
        }
        String budget = firstText(request.budget(), "").toLowerCase();
        return containsAny(budget, List.of("절약", "저예산", "low", "budget"));
    }

    private String trustMessage(int candidateCount, int targetStopCount) {
        if (candidateCount < targetStopCount) {
            return "선택한 지역의 검증된 장소가 충분하지 않아 가능한 장소만으로 코스를 구성했습니다.";
        }
        return "검증된 sceniK 장소로만 구성한 코스입니다.";
    }

    private String placeReason(CandidatePlace place, String style) {
        String scene = firstText(place.getSceneDescription(), "");
        if (!scene.isBlank() && !isGenericTravelTip(scene)) {
            return trimSentence(scene, 74);
        }
        String contentTitle = contentTitleForDisplay(place);
        String category = placeCategory(place);
        if (!contentTitle.isBlank()) {
            return contentTitle + "의 분위기와 이어지는 " + category + " 코스 포인트입니다.";
        }
        return firstText(style, "감성 여행") + "에 맞춰 " + place.getName() + "의 분위기를 둘러보기 좋은 장소입니다.";
    }

    private String usefulTravelTip(String proposed, CandidatePlace place, int dayIndex, int placeIndex, int dayPlaceCount) {
        String cleaned = firstText(proposed, "").trim();
        if (!cleaned.isBlank() && !isGenericTravelTip(cleaned)) {
            return trimSentence(cleaned, 82);
        }
        String region = firstText(place.getRegion(), "해당 지역");
        if (placeIndex == 0) {
            return place.getName() + "에서 하루를 시작하면 " + region + " 동선을 잡기 좋습니다.";
        }
        if (placeIndex == dayPlaceCount - 1) {
            return place.getName() + "에서 여유 있게 마무리하고 주변 식사나 휴식 시간을 잡아보세요.";
        }
        return place.getName() + " 주변을 짧게 둘러본 뒤 다음 장소로 이어가면 동선이 자연스럽습니다.";
    }

    private boolean isGenericTravelTip(String value) {
        String text = value == null ? "" : value.toLowerCase();
        return text.isBlank()
                || text.contains("opening hours")
                || text.contains("transit")
                || text.contains("운영 시간")
                || text.contains("현지 이동편")
                || text.contains("방문 전")
                || text.contains("숙소(");
    }

    private String contentTitleForDisplay(CandidatePlace place) {
        String title = firstText(place.getRelatedContentTitle(), "");
        return looksLikeContentId(title) ? "" : title;
    }

    private String trimSentence(String value, int maxLength) {
        String text = value == null ? "" : value.replaceAll("\\s+", " ").trim();
        if (text.length() <= maxLength) return text;
        return text.substring(0, Math.max(0, maxLength - 3)).trim() + "...";
    }

    private String normalizedRecommendedTime(String proposed, int dayIndex, int index) {
        String time = firstText(proposed, "").trim();
        if (!time.isBlank() && !isTemplateTime(time)) return time;
        return recommendedTime(dayIndex, index);
    }

    private boolean isTemplateTime(String time) {
        return List.of("10:00", "12:30", "15:30", "18:30").contains(time);
    }

    private String recommendedTime(int dayIndex, int index) {
        String[][] slots = {
                {"09:30", "11:40", "14:20", "17:10"},
                {"10:00", "13:00", "15:40", "18:00"},
                {"09:40", "12:10", "14:50", "17:30"}
        };
        String[] daySlots = slots[Math.floorMod(dayIndex, slots.length)];
        return daySlots[Math.min(index, daySlots.length - 1)];
    }

    private String estimateTravelTime(List<RouteStopDto> stops) {
        int minutes = stops.stream()
                .map(RouteStopDto::estimatedTravelMinutes)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        return "이동 약 " + minutes + "분";
    }

    private int positiveOrDefault(Integer value, int fallback) {
        return value == null || value <= 0 ? fallback : value;
    }

    private String buildSummary(AiRouteRequest request, String contentTitle, String region, int dayCount) {
        String dateText = firstText(request.dateRangeLabel(), dayCount + "일");
        String transport = firstText(request.transport(), "이동 조건");
        String style = styleLabel(request);
        String hotel = request.hotelAddress() == null || request.hotelAddress().isBlank() ? "" : " 숙소 위치를 출발·복귀 기준으로 참고했습니다.";
        return "선택한 콘텐츠와 여행 조건을 바탕으로 " + region + "에서 " + dateText + " 동안 " + transport + ", " + style + "에 맞춰 구성한 코스입니다." + hotel;
    }

    private String firstText(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? fallback : preferred;
    }

    private String normalizeBaseUrl(String baseUrl) {
        String value = baseUrl == null || baseUrl.isBlank() ? "https://gms.ssafy.io/gmsapi/" : baseUrl.trim();
        return value.endsWith("/") ? value : value + "/";
    }

    private record DayAreaBucket(List<AreaStopGroup> groups) {
    }

    private record AreaBucketMerge(int leftIndex, int rightIndex, double score) {
    }

    private record GenerationState(AiRouteRequest request, AtomicInteger regenerateCount) {
    }

    private record StopPolicy(int minPlacesPerDay, int preferredPlacesPerDay, int maxPlacesPerDay) {
    }

    private record AreaCluster(String id, String label) {
    }

    private record AreaStopGroup(String areaId, String areaLabel, List<RouteStopDto> stops) {
        RouteStopDto centroidStop() {
            if (stops == null || stops.isEmpty()) {
                return null;
            }
            double latitude = stops.stream()
                    .filter(stop -> stop.latitude() != null)
                    .mapToDouble(RouteStopDto::latitude)
                    .average()
                    .orElse(stops.get(0).latitude() == null ? 0.0 : stops.get(0).latitude());
            double longitude = stops.stream()
                    .filter(stop -> stop.longitude() != null)
                    .mapToDouble(RouteStopDto::longitude)
                    .average()
                    .orElse(stops.get(0).longitude() == null ? 0.0 : stops.get(0).longitude());
            RouteStopDto first = stops.get(0);
            return new RouteStopDto(
                    null,
                    first.placeId(),
                    first.name(),
                    first.placeName(),
                    first.dayNumber(),
                    first.visitOrder(),
                    first.address(),
                    latitude,
                    longitude,
                    first.thumbnailUrl(),
                    first.category(),
                    first.recommendedTime(),
                    first.recommendReason(),
                    first.reason(),
                    first.estimatedStayMinutes(),
                    first.estimatedTravelMinutes(),
                    first.travelTip()
            );
        }
    }

    private enum TransportProfile {
        WALK("walk", 4.0),
        TRANSIT("transit", 24.0),
        CAR("car", 35.0);

        private final String label;
        private final double averageSpeedKph;

        TransportProfile(String label, double averageSpeedKph) {
            this.label = label;
            this.averageSpeedKph = averageSpeedKph;
        }

        String label() {
            return label;
        }

        double averageSpeedKph() {
            return averageSpeedKph;
        }
    }

    private record GmsChatRequest(String model, List<GmsMessage> messages, Map<String, String> response_format) {
    }

    private record GmsMessage(String role, String content) {
    }

    private record GmsChatResponse(List<GmsChoice> choices) {
    }

    private record GmsChoice(GmsMessage message) {
        String content() {
            return message == null ? null : message.content();
        }
    }

    private record AiRoutePlan(String summary, List<AiRoutePlanDay> days) {
    }

    private record AiRoutePlanDay(Integer dayNumber, String estimatedTravelTime, List<AiRoutePlanStop> places) {
    }

    private record AiRoutePlanStop(
            String placeId,
            Integer visitOrder,
            String recommendedTime,
            String recommendReason,
            Integer estimatedStayMinutes,
            Integer estimatedTravelMinutes,
            String travelTip
    ) {
    }
}
