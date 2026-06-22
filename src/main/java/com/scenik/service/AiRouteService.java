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

    private final AiRouteMapper aiRouteMapper;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String gmsBaseUrl;
    private final String gmsApiKey;
    private final String gmsModel;
    private final Map<String, GenerationState> generationCache = new ConcurrentHashMap<>();

    public AiRouteService(
            AiRouteMapper aiRouteMapper,
            ObjectMapper objectMapper,
            @Value("${scenik.ai.gms.base-url}") String gmsBaseUrl,
            @Value("${scenik.ai.gms.api-key:}") String gmsApiKey,
            @Value("${scenik.ai.gms.model:gpt-5.4-mini}") String gmsModel,
            @Value("${scenik.ai.gms.connect-timeout-ms:1000}") int connectTimeoutMs,
            @Value("${scenik.ai.gms.read-timeout-ms:5000}") int readTimeoutMs
    ) {
        this.aiRouteMapper = aiRouteMapper;
        this.objectMapper = objectMapper;
        this.gmsBaseUrl = normalizeBaseUrl(gmsBaseUrl);
        this.gmsApiKey = gmsApiKey == null ? "" : gmsApiKey.trim();
        this.gmsModel = gmsModel;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public AiRouteDto generateRoute(AiRouteRequest request) {
        AiRouteRequest normalizedRequest = request == null ? new AiRouteRequest(null, List.of(), null, List.of(), 1, null, null, null, null, null, null, List.of(), null, null, null, null) : request;
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
        int targetCount = Math.max(36, requestedDays * 10);
        List<String> regions = normalizeRegions(request);
        String regionLabel = regionLabel(regions);
        List<CandidatePlace> selectedContentCandidates = deduplicate(aiRouteMapper.findCandidatePlaces(contentIds, null, targetCount));
        logSelectedContentCandidateCounts(contentIds, regions, selectedContentCandidates);
        List<CandidatePlace> candidates = filterByRegions(selectedContentCandidates, regions);
        int regionFilteredCount = candidates.size();
        int targetStopCount = targetStopCount(requestedDays);
        int minimumStopCount = minimumStopCount(requestedDays);
        candidates = supplementCandidatePlaces(candidates, selectedContentCandidates, regions, targetStopCount, targetCount);
        log.info(
                "AI route candidate filtering contentIds={} selectedRegions={} contentPlaceCount={} regionFilteredCount={} finalPromptCount={}",
                contentIds,
                regions,
                selectedContentCandidates.size(),
                regionFilteredCount,
                candidates.size()
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
        List<RouteDayDto> days = generateAiDays(request, routeCandidates, dayCount)
                .orElseGet(() -> buildDeterministicDays(routeCandidates, dayCount, style, request.hotelAddress()));
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

    private Optional<List<RouteDayDto>> generateAiDays(AiRouteRequest request, List<CandidatePlace> candidates, int dayCount) {
        if (gmsApiKey.isBlank()) {
            log.info("GMS API key is not configured. Falling back to deterministic AI route generation.");
            return Optional.empty();
        }
        try {
            GmsChatResponse response = restClient.post()
                    .uri(URI.create(gmsBaseUrl + "api.openai.com/v1/chat/completions"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + gmsApiKey)
                    .body(buildGmsRequest(request, candidates, dayCount))
                    .retrieve()
                    .body(GmsChatResponse.class);
            String content = response == null || response.choices() == null || response.choices().isEmpty()
                    ? null
                    : response.choices().get(0).content();
            AiRoutePlan plan = parseAiRoutePlan(content);
            return validateAndBuildAiDays(plan, candidates, dayCount);
        } catch (RestClientException | IllegalArgumentException | JsonProcessingException exception) {
            log.warn("GMS AI route generation failed. Falling back to deterministic route. reason={}", exception.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    private GmsChatRequest buildGmsRequest(AiRouteRequest request, List<CandidatePlace> candidates, int dayCount) throws JsonProcessingException {
        int minPlacesPerDay = minPlacesPerDay(candidates, dayCount);
        int preferredPlacesPerDay = preferredPlacesPerDay(candidates, dayCount);
        Map<String, Object> routeContext = new LinkedHashMap<>();
        routeContext.put("contentIds", normalizeContentIds(request));
        routeContext.put("region", regionLabel(normalizeRegions(request)));
        routeContext.put("regions", normalizeRegions(request));
        routeContext.put("durationDays", dayCount);
        routeContext.put("minPlacesPerDay", minPlacesPerDay);
        routeContext.put("preferredPlacesPerDay", preferredPlacesPerDay);
        routeContext.put("targetStopCount", Math.min(candidates.size(), targetStopCount(dayCount)));
        routeContext.put("startDate", request.startDate());
        routeContext.put("endDate", request.endDate());
        routeContext.put("dateRangeLabel", request.dateRangeLabel());
        routeContext.put("transport", request.transport());
        routeContext.put("companion", request.companion());
        routeContext.put("style", request.style());
        routeContext.put("travelStyles", normalizeTravelStyles(request));
        routeContext.put("budgetMin", request.budgetMin());
        routeContext.put("budgetMax", request.budgetMax());
        routeContext.put("budget", request.budget());
        routeContext.put("hotelAddress", request.hotelAddress());
        routeContext.put("candidatePlaces", candidates.stream().map(this::promptCandidate).toList());
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
                - 정확히 durationDays 개의 day를 생성하세요. 2박 3일이면 반드시 3일차까지 생성하세요.
                - 각 day는 최소 minPlacesPerDay개 이상의 장소를 포함하세요.
                - 가능한 한 preferredPlacesPerDay에 가깝게 장소를 배치하세요.
                - 후보가 충분하면 1일 코스는 3~5개, 2일 코스는 하루 3~4개, 3일 코스는 하루 2~4개 장소로 구성하세요.
                - 매일 같은 시간표를 반복하지 말고 오전, 점심 전후, 오후, 저녁 흐름이 자연스럽게 보이도록 시간을 다르게 배치하세요.
                - visitWorthyCategories에 가까운 장소를 우선하고, 학교/호텔/일반 상점은 콘텐츠 연결성이 강하거나 대체 후보가 없을 때만 사용하세요.
                - recommendReason은 장소별로 한 문장만 작성하고, contentId나 DB 검증 정책을 쓰지 마세요.
                - travelTip에는 운영 시간 확인 같은 공통 경고를 반복하지 말고, 장소 주변 동선이나 휴식 포인트가 있을 때만 짧게 작성하세요.
                - 장소 간 이동 흐름이 자연스럽도록 가까운 지역끼리 묶으세요.
                - hotelAddress가 비어 있지 않으면 전체 동선 판단에만 참고하고, 모든 장소 설명에 숙소 문구를 반복하지 마세요.
                - budget, region, style, travelStyles, transport, companion 조건을 추천 이유와 이동 팁에 반영하세요.
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

    private Optional<List<RouteDayDto>> validateAndBuildAiDays(AiRoutePlan plan, List<CandidatePlace> candidates, int dayCount) {
        if (plan == null || plan.days() == null || plan.days().size() != dayCount) {
            return Optional.empty();
        }
        int minPlacesPerDay = minPlacesPerDay(candidates, dayCount);
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
            if (stops.size() < minPlacesPerDay) {
                log.info("AI route response rejected by regional validation removedPlaceCount={}", removedPlaces);
                return Optional.empty();
            }
            days.add(new RouteDayDto(dayIndex + 1, firstText(planDay.estimatedTravelTime(), estimateTravelTime(stops)), stops));
        }

        log.info("AI route response regional validation removedPlaceCount={}", removedPlaces);
        return Optional.of(days);
    }

    private List<RouteDayDto> buildDeterministicDays(List<CandidatePlace> candidates, int dayCount, String style, String hotelAddress) {
        List<List<CandidatePlace>> groups = new ArrayList<>();
        for (int i = 0; i < dayCount; i++) {
            groups.add(new ArrayList<>());
        }
        int maxStopCount = Math.min(candidates.size(), Math.max(dayCount, Math.min(candidates.size(), dayCount * 4)));
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

    private int minPlacesPerDay(List<CandidatePlace> candidates, int dayCount) {
        return candidates.size() >= dayCount * 2 ? 2 : 1;
    }

    private int preferredPlacesPerDay(List<CandidatePlace> candidates, int dayCount) {
        return candidates.size() >= dayCount * 3 ? 3 : minPlacesPerDay(candidates, dayCount);
    }

    private int targetStopCount(int requestedDays) {
        if (requestedDays <= 1) return 4;
        if (requestedDays == 2) return 6;
        return requestedDays * 3;
    }

    private int minimumStopCount(int requestedDays) {
        if (requestedDays <= 1) return 3;
        if (requestedDays == 2) return 6;
        return requestedDays * 2;
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

    private record GenerationState(AiRouteRequest request, AtomicInteger regenerateCount) {
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
