package com.scenik.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scenik.domain.CandidatePlace;
import com.scenik.domain.RoutePlace;
import com.scenik.dto.RouteDtos.AiRouteDto;
import com.scenik.dto.RouteDtos.AiRouteRequest;
import com.scenik.dto.RouteDtos.CandidatePlaceDto;
import com.scenik.dto.RouteDtos.RouteStopDto;
import com.scenik.mapper.AiRouteMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiRouteServiceTest {
    @Mock AiRouteMapper aiRouteMapper;

    private AiRouteService aiRouteService;

    @BeforeEach
    void setUp() {
        aiRouteService = new AiRouteService(
                aiRouteMapper,
                new ObjectMapper(),
                "https://gms.ssafy.io/gmsapi/",
                "",
                "gpt-test",
                100,
                100,
                2,
                3,
                6
        );
    }

    @Test
    void sparseSelectedContentCandidatesAreSupplementedFromSameRegion() {
        List<String> contentIds = List.of("C_DRAMA_001");
        AiRouteRequest request = new AiRouteRequest(
                null,
                contentIds,
                "Seoul",
                List.of("Seoul"),
                2,
                null,
                null,
                null,
                "walk",
                List.of("PUBLIC_TRANSIT"),
                List.of(),
                null,
                "cinematic",
                List.of("cinematic"),
                null,
                null,
                null,
                null
        );
        List<CandidatePlace> selectedContentPlaces = List.of(
                place("P_001", "C_DRAMA_001", "Drama", "Seoul"),
                place("P_002", "C_DRAMA_001", "Drama", "Seoul")
        );
        List<CandidatePlace> regionalPlaces = List.of(
                place("P_001", "C_DRAMA_001", "Drama", "Seoul"),
                place("P_002", "C_DRAMA_001", "Drama", "Seoul"),
                place("P_003", "C_DRAMA_002", "Supplement", "Seoul"),
                place("P_004", "C_DRAMA_002", "Supplement", "Seoul"),
                place("P_005", "C_DRAMA_003", "Supplement", "Seoul"),
                place("P_006", "C_DRAMA_003", "Supplement", "Seoul")
        );

        when(aiRouteMapper.findCandidatePlaces(eq(contentIds), isNull(), anyInt())).thenReturn(selectedContentPlaces);
        when(aiRouteMapper.findCandidatePlaces(isNull(), eq("Seoul"), anyInt())).thenReturn(regionalPlaces);

        AiRouteDto route = aiRouteService.generateRoute(request);

        assertThat(route.days()).hasSize(2);
        assertThat(route.places()).hasSize(6);
        assertThat(route.days().get(0).places().get(0).category()).isEqualTo("spot");
        assertThat(route.days()).allSatisfy(day -> assertThat(day.places()).hasSize(3));
        List<String> dayOneTimes = route.days().get(0).places().stream().map(RouteStopDto::recommendedTime).toList();
        List<String> dayTwoTimes = route.days().get(1).places().stream().map(RouteStopDto::recommendedTime).toList();
        assertThat(dayTwoTimes).isNotEqualTo(dayOneTimes);
        assertThat(route.candidatePlaces()).hasSize(6);
        verify(aiRouteMapper).findCandidatePlaces(isNull(), eq("Seoul"), anyInt());
    }


    @Test
    void threeDayRouteUsesMultipleStopsAndVariedDailyTimes() {
        List<String> contentIds = List.of("C_DRAMA_010");
        AiRouteRequest request = new AiRouteRequest(
                null,
                contentIds,
                "Seoul",
                List.of("Seoul"),
                3,
                null,
                null,
                null,
                "walk",
                List.of("PUBLIC_TRANSIT"),
                List.of(),
                null,
                "cinematic",
                List.of("cinematic"),
                null,
                null,
                null,
                null
        );
        List<CandidatePlace> selectedContentPlaces = List.of(
                place("P_101", "C_DRAMA_010", "Drama", "Seoul"),
                place("P_102", "C_DRAMA_010", "Drama", "Seoul"),
                place("P_103", "C_DRAMA_010", "Drama", "Seoul")
        );
        List<CandidatePlace> regionalPlaces = List.of(
                place("P_101", "C_DRAMA_010", "Drama", "Seoul"),
                place("P_102", "C_DRAMA_010", "Drama", "Seoul"),
                place("P_103", "C_DRAMA_010", "Drama", "Seoul"),
                place("P_104", "C_DRAMA_011", "Supplement", "Seoul"),
                place("P_105", "C_DRAMA_011", "Supplement", "Seoul"),
                place("P_106", "C_DRAMA_012", "Supplement", "Seoul"),
                place("P_107", "C_DRAMA_012", "Supplement", "Seoul"),
                place("P_108", "C_DRAMA_013", "Supplement", "Seoul"),
                place("P_109", "C_DRAMA_013", "Supplement", "Seoul")
        );

        when(aiRouteMapper.findCandidatePlaces(eq(contentIds), isNull(), anyInt())).thenReturn(selectedContentPlaces);
        when(aiRouteMapper.findCandidatePlaces(isNull(), eq("Seoul"), anyInt())).thenReturn(regionalPlaces);

        AiRouteDto route = aiRouteService.generateRoute(request);

        assertThat(route.days()).hasSize(3);
        assertThat(route.places()).hasSize(9);
        assertThat(route.days()).allSatisfy(day -> assertThat(day.places()).hasSize(3));
        assertThat(route.days().get(1).places().stream().map(RouteStopDto::recommendedTime).toList())
                .isNotEqualTo(route.days().get(0).places().stream().map(RouteStopDto::recommendedTime).toList());
        assertThat(route.days().get(2).places().stream().map(RouteStopDto::recommendedTime).toList())
                .isNotEqualTo(route.days().get(0).places().stream().map(RouteStopDto::recommendedTime).toList());
    }

    @Test
    void seoulMultiDayRouteSupplementsPromptCandidatesBeyondMinimumStops() {
        List<String> contentIds = List.of("C_DRAMA_SEOUL");
        AiRouteRequest request = aiRequest(contentIds, "Seoul", 4, "public transit", List.of("PUBLIC_TRANSIT"), List.of());
        List<CandidatePlace> selectedContentPlaces = IntStream.rangeClosed(1, 13)
                .mapToObj(index -> place(
                        String.format("P_SEOUL_%02d", index),
                        "C_DRAMA_SEOUL",
                        "Selected Seoul Drama",
                        "Seoul",
                        37.5000 + index * 0.001,
                        127.0000 + index * 0.001
                ))
                .toList();
        List<CandidatePlace> regionalPlaces = IntStream.rangeClosed(1, 30)
                .mapToObj(index -> place(
                        String.format("P_SEOUL_%02d", index),
                        index <= 13 ? "C_DRAMA_SEOUL" : "C_DRAMA_SUPPLEMENT",
                        index <= 13 ? "Selected Seoul Drama" : "Supplement Seoul Place",
                        "Seoul",
                        37.5000 + index * 0.001,
                        127.0000 + index * 0.001
                ))
                .toList();

        when(aiRouteMapper.findCandidatePlaces(eq(contentIds), isNull(), anyInt())).thenReturn(selectedContentPlaces);
        when(aiRouteMapper.findCandidatePlaces(isNull(), eq("Seoul"), anyInt())).thenReturn(regionalPlaces);

        AiRouteDto route = aiRouteService.generateRoute(request);

        assertThat(route.candidatePlaces()).hasSize(30);
        assertThat(route.candidatePlaces()).extracting(CandidatePlaceDto::placeId).doesNotContain("P_0252");
        verify(aiRouteMapper).findCandidatePlaces(isNull(), eq("Seoul"), anyInt());
    }

    @Test
    void carRouteCanUseFourStopsPerDay() {
        List<String> contentIds = List.of("C_DRAMA_020");
        AiRouteRequest request = new AiRouteRequest(
                null,
                contentIds,
                "Seoul",
                List.of("Seoul"),
                2,
                null,
                null,
                null,
                "car",
                List.of("CAR"),
                List.of(1, 2),
                "friends",
                "active",
                List.of("active"),
                null,
                500000,
                "standard",
                null
        );
        List<CandidatePlace> places = List.of(
                place("P_201", "C_DRAMA_020", "Drama", "Seoul", 37.50, 127.00),
                place("P_202", "C_DRAMA_020", "Drama", "Seoul", 37.51, 127.00),
                place("P_203", "C_DRAMA_020", "Drama", "Seoul", 37.52, 127.00),
                place("P_204", "C_DRAMA_020", "Drama", "Seoul", 37.53, 127.00),
                place("P_205", "C_DRAMA_020", "Drama", "Seoul", 37.54, 127.00),
                place("P_206", "C_DRAMA_020", "Drama", "Seoul", 37.55, 127.00),
                place("P_207", "C_DRAMA_020", "Drama", "Seoul", 37.56, 127.00),
                place("P_208", "C_DRAMA_020", "Drama", "Seoul", 37.57, 127.00)
        );

        when(aiRouteMapper.findCandidatePlaces(eq(contentIds), isNull(), anyInt())).thenReturn(places);

        AiRouteDto route = aiRouteService.generateRoute(request);

        assertThat(route.places()).hasSize(8);
        assertThat(route.days()).allSatisfy(day -> assertThat(day.places()).hasSize(4));
    }

    @Test
    void routeStopsAreReorderedByNearestNeighborDistance() {
        List<String> contentIds = List.of("C_DRAMA_030");
        AiRouteRequest request = new AiRouteRequest(
                null,
                contentIds,
                "Seoul",
                List.of("Seoul"),
                1,
                null,
                null,
                null,
                "walk",
                List.of("WALK"),
                List.of(),
                null,
                "cinematic",
                List.of("cinematic"),
                null,
                null,
                null,
                null
        );
        List<CandidatePlace> places = List.of(
                place("P_A", "C_DRAMA_030", "Drama", "Seoul", 37.00, 127.00),
                place("P_B", "C_DRAMA_030", "Drama", "Seoul", 37.01, 127.04),
                place("P_C", "C_DRAMA_030", "Drama", "Seoul", 37.02, 127.01)
        );

        when(aiRouteMapper.findCandidatePlaces(eq(contentIds), isNull(), anyInt())).thenReturn(places);

        AiRouteDto route = aiRouteService.generateRoute(request);

        assertThat(route.days()).hasSize(1);
        assertThat(route.days().get(0).places()).extracting(RouteStopDto::placeId)
                .containsExactly("P_A", "P_C", "P_B");
        assertThat(route.days().get(0).places()).extracting(RouteStopDto::visitOrder)
                .containsExactly(1, 2, 3);
        assertThat(route.days().get(0).places().get(1).estimatedTravelMinutes()).isPositive();
    }

    @Test
    void routeDaysAreGroupedByAreaBeforeDistanceOptimization() {
        List<String> contentIds = List.of("C_DRAMA_040");
        AiRouteRequest request = new AiRouteRequest(
                null,
                contentIds,
                "서울",
                List.of("서울"),
                4,
                null,
                null,
                null,
                "car",
                List.of("CAR"),
                List.of(2),
                "friends",
                "active",
                List.of("active"),
                null,
                900000,
                "standard",
                null
        );
        List<CandidatePlace> places = List.of(
                place("P_JJ_1", "C_DRAMA_040", "Drama", "서울", "서울 종로구 종로 1", 37.570, 126.980),
                place("P_JJ_2", "C_DRAMA_040", "Drama", "서울", "서울 종로구 인사동 2", 37.571, 126.981),
                place("P_JJ_3", "C_DRAMA_040", "Drama", "서울", "서울 종로구 세종대로 3", 37.572, 126.982),
                place("P_GN_1", "C_DRAMA_040", "Drama", "서울", "서울 강남구 도산대로 1", 37.520, 127.040),
                place("P_GN_2", "C_DRAMA_040", "Drama", "서울", "서울 강남구 청담동 2", 37.521, 127.041),
                place("P_GN_3", "C_DRAMA_040", "Drama", "서울", "서울 강남구 압구정로 3", 37.522, 127.042),
                place("P_GN_4", "C_DRAMA_040", "Drama", "서울", "서울 강남구 삼성동 4", 37.523, 127.043),
                place("P_GN_5", "C_DRAMA_040", "Drama", "서울", "서울 강남구 역삼동 5", 37.524, 127.044),
                place("P_GN_6", "C_DRAMA_040", "Drama", "서울", "서울 강남구 신사동 6", 37.525, 127.045),
                place("P_SD_1", "C_DRAMA_040", "Drama", "서울", "서울 성동구 성수동 1", 37.545, 127.055),
                place("P_SD_2", "C_DRAMA_040", "Drama", "서울", "서울 성동구 서울숲 2", 37.546, 127.056),
                place("P_SD_3", "C_DRAMA_040", "Drama", "서울", "서울 성동구 뚝섬로 3", 37.547, 127.057),
                place("P_SD_4", "C_DRAMA_040", "Drama", "서울", "서울 성동구 왕십리로 4", 37.548, 127.058),
                place("P_SD_5", "C_DRAMA_040", "Drama", "서울", "서울 성동구 성수이로 5", 37.549, 127.059),
                place("P_SP_1", "C_DRAMA_040", "Drama", "서울", "서울 송파구 잠실동 1", 37.510, 127.100),
                place("P_SP_2", "C_DRAMA_040", "Drama", "서울", "서울 송파구 석촌동 2", 37.511, 127.101),
                place("P_SP_3", "C_DRAMA_040", "Drama", "서울", "서울 송파구 올림픽로 3", 37.512, 127.102),
                place("P_SP_4", "C_DRAMA_040", "Drama", "서울", "서울 송파구 방이동 4", 37.513, 127.103)
        );

        when(aiRouteMapper.findCandidatePlaces(eq(contentIds), isNull(), anyInt())).thenReturn(places);

        AiRouteDto route = aiRouteService.generateRoute(request);

        assertThat(route.days()).hasSize(4);
        assertThat(route.days().get(0).places()).hasSizeLessThanOrEqualTo(4);
        assertThat(route.days().get(1).places()).hasSizeGreaterThanOrEqualTo(5);
        assertThat(route.days().get(2).places()).hasSizeGreaterThanOrEqualTo(5);
        assertThat(route.days().get(3).places()).hasSizeLessThanOrEqualTo(4);
        route.days().forEach(day -> assertThat(districts(day.places())).hasSize(1));
    }

    @Test
    void gangwonThreeDayRouteDoesNotMixDistantCitiesInOneDay() {
        List<String> contentIds = List.of("C_DRAMA_GANGWON");
        AiRouteRequest request = new AiRouteRequest(
                null,
                contentIds,
                "Gangwon",
                List.of("Gangwon"),
                3,
                null,
                null,
                null,
                "public transit",
                List.of("PUBLIC_TRANSIT"),
                List.of(1),
                "friends",
                "active",
                List.of("active"),
                null,
                600000,
                "standard",
                null
        );
        List<CandidatePlace> places = List.of(
                place("P_YW_1", "C_DRAMA_GANGWON", "Gangwon Drama", "Gangwon", "Gangwon Yeongwol-gun Cheongnyeongpo", 37.1830, 128.4610),
                place("P_YW_2", "C_DRAMA_GANGWON", "Gangwon Drama", "Gangwon", "Gangwon Yeongwol-gun Jangneung", 37.1970, 128.4510),
                place("P_JS_1", "C_DRAMA_GANGWON", "Gangwon Drama", "Gangwon", "Gangwon Jeongseon-gun Molundae", 37.3790, 128.6630),
                place("P_PC_1", "C_DRAMA_GANGWON", "Gangwon Drama", "Gangwon", "Gangwon Pyeongchang-gun Balwangsan", 37.3700, 128.3900),
                place("P_PC_2", "C_DRAMA_GANGWON", "Gangwon Drama", "Gangwon", "Gangwon Pyeongchang-gun Daegwallyeong", 37.6800, 128.7600),
                place("P_GN_1", "C_DRAMA_GANGWON", "Gangwon Drama", "Gangwon", "Gangwon Gangneung-si Jumunjin", 37.9120, 128.8170),
                place("P_GN_2", "C_DRAMA_GANGWON", "Gangwon Drama", "Gangwon", "Gangwon Gangneung-si Anmok Beach", 37.7720, 128.9480),
                place("P_YY_1", "C_DRAMA_GANGWON", "Gangwon Drama", "Gangwon", "Gangwon Yangyang-gun Naksansa", 38.1240, 128.6270),
                place("P_SC_1", "C_DRAMA_GANGWON", "Gangwon Drama", "Gangwon", "Gangwon Sokcho-si Seorak", 38.1660, 128.4890),
                place("P_SM_1", "C_DRAMA_GANGWON", "Gangwon Drama", "Gangwon", "Gangwon Samcheok-si Beach", 37.4500, 129.1660),
                place("P_HC_1", "C_DRAMA_GANGWON", "Gangwon Drama", "Gangwon", "Gangwon Hongcheon-gun Vivaldi", 37.6500, 127.6870),
                place("P_HC_2", "C_DRAMA_GANGWON", "Gangwon Drama", "Gangwon", "Gangwon Hongcheon-gun River", 37.7000, 127.8800),
                place("P_CC_1", "C_DRAMA_GANGWON", "Gangwon Drama", "Gangwon", "Gangwon Chuncheon-si Nami Island", 37.7910, 127.5250),
                place("P_CC_2", "C_DRAMA_GANGWON", "Gangwon Drama", "Gangwon", "Gangwon Chuncheon-si Animation Museum", 37.8930, 127.7230),
                place("P_DB_1", "C_DRAMA_GANGWON", "Gangwon Drama", "Gangwon", "Gangwon Donghae-si Mureung", 37.5200, 129.1150)
        );

        when(aiRouteMapper.findCandidatePlaces(eq(contentIds), isNull(), anyInt())).thenReturn(places);

        AiRouteDto route = aiRouteService.generateRoute(request);

        assertThat(route.days()).hasSize(3);
        route.days().forEach(day -> assertThat(totalDistanceKm(day.places())).isLessThan(190.0));
        route.days().forEach(day -> assertThat(cityTokens(day.places())).hasSizeLessThanOrEqualTo(3));
        route.days().forEach(day -> assertThat(containsAllCities(day.places(), "Gangneung-si", "Yangyang-gun", "Jeongseon-gun", "Samcheok-si", "Hongcheon-gun")).isFalse());
        route.days().forEach(day -> assertThat(containsAllCities(day.places(), "Pyeongchang-gun", "Sokcho-si", "Chuncheon-si")).isFalse());
        route.days().forEach(day -> assertThat(containsAllCities(day.places(), "Samcheok-si", "Hongcheon-gun")).isFalse());
        route.days().forEach(day -> assertThat(containsAllCities(day.places(), "Gangneung-si", "Chuncheon-si")).isFalse());
    }

    @Test
    void llmNineStopDayIsRepairedBelowAbsoluteLimit() throws Exception {
        List<String> contentIds = List.of("C_DRAMA_COMPACT");
        List<CandidatePlace> places = List.of(
                place("P_C_01", "C_DRAMA_COMPACT", "Compact Drama", "Seoul", 37.5000, 127.0000),
                place("P_C_02", "C_DRAMA_COMPACT", "Compact Drama", "Seoul", 37.5010, 127.0010),
                place("P_C_03", "C_DRAMA_COMPACT", "Compact Drama", "Seoul", 37.5020, 127.0020),
                place("P_C_04", "C_DRAMA_COMPACT", "Compact Drama", "Seoul", 37.5030, 127.0030),
                place("P_C_05", "C_DRAMA_COMPACT", "Compact Drama", "Seoul", 37.5040, 127.0040),
                place("P_C_06", "C_DRAMA_COMPACT", "Compact Drama", "Seoul", 37.5050, 127.0050),
                place("P_C_07", "C_DRAMA_COMPACT", "Compact Drama", "Seoul", 37.5060, 127.0060),
                place("P_C_08", "C_DRAMA_COMPACT", "Compact Drama", "Seoul", 37.5070, 127.0070),
                place("P_C_09", "C_DRAMA_COMPACT", "Compact Drama", "Seoul", 37.5080, 127.0080)
        );
        HttpServer server = startGmsStub(aiRouteJson("Compact repaired route", List.of(
                List.of("P_C_01", "P_C_02", "P_C_03", "P_C_04", "P_C_05", "P_C_06", "P_C_07", "P_C_08", "P_C_09")
        )));

        try {
            AiRouteService service = aiRouteServiceWithGms(server, 9);
            AiRouteRequest request = aiRequest(contentIds, "Seoul", 1, "car", List.of("CAR"), List.of(1));
            when(aiRouteMapper.findCandidatePlaces(eq(contentIds), isNull(), anyInt())).thenReturn(places);

            AiRouteDto route = service.generateRoute(request);

            assertThat(route.days()).hasSize(1);
            assertThat(route.days().get(0).places()).hasSizeLessThanOrEqualTo(7);
            assertThat(route.days().get(0).places()).extracting(RouteStopDto::placeId).doesNotHaveDuplicates();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void publicTransitScatteredLlmDayIsReducedForFeasibility() throws Exception {
        List<String> contentIds = List.of("C_DRAMA_SCATTERED");
        List<CandidatePlace> places = List.of(
                place("P_S_01", "C_DRAMA_SCATTERED", "Scattered Drama", "Gangwon", "Gangwon Gangneung-si Jumunjin", 37.9120, 128.8170),
                place("P_S_02", "C_DRAMA_SCATTERED", "Scattered Drama", "Gangwon", "Gangwon Yangyang-gun Naksansa", 38.1240, 128.6270),
                place("P_S_03", "C_DRAMA_SCATTERED", "Scattered Drama", "Gangwon", "Gangwon Jeongseon-gun Molundae", 37.3790, 128.6630),
                place("P_S_04", "C_DRAMA_SCATTERED", "Scattered Drama", "Gangwon", "Gangwon Samcheok-si Beach", 37.4500, 129.1660),
                place("P_S_05", "C_DRAMA_SCATTERED", "Scattered Drama", "Gangwon", "Gangwon Hongcheon-gun Vivaldi", 37.6500, 127.6870),
                place("P_S_06", "C_DRAMA_SCATTERED", "Scattered Drama", "Gangwon", "Gangwon Chuncheon-si Nami Island", 37.7910, 127.5250),
                place("P_S_07", "C_DRAMA_SCATTERED", "Scattered Drama", "Gangwon", "Gangwon Sokcho-si Seorak", 38.1660, 128.4890),
                place("P_S_08", "C_DRAMA_SCATTERED", "Scattered Drama", "Gangwon", "Gangwon Pyeongchang-gun Balwangsan", 37.3700, 128.3900)
        );
        HttpServer server = startGmsStub(aiRouteJson("Scattered repaired route", List.of(
                List.of("P_S_01", "P_S_02", "P_S_03", "P_S_04", "P_S_05", "P_S_06", "P_S_07", "P_S_08")
        )));

        try {
            AiRouteService service = aiRouteServiceWithGms(server, 9);
            AiRouteRequest request = aiRequest(contentIds, "Gangwon", 1, "public transit", List.of("PUBLIC_TRANSIT"), List.of());
            when(aiRouteMapper.findCandidatePlaces(eq(contentIds), isNull(), anyInt())).thenReturn(places);

            AiRouteDto route = service.generateRoute(request);

            assertThat(route.days()).hasSize(1);
            assertThat(route.days().get(0).places()).hasSizeLessThanOrEqualTo(5);
            assertThat(containsAllCities(route.days().get(0).places(), "Gangneung-si", "Yangyang-gun", "Jeongseon-gun", "Samcheok-si", "Hongcheon-gun")).isFalse();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void llmFourOneSevenOneDayPatternIsRebalancedWhenPlacesAreCompact() throws Exception {
        List<String> contentIds = List.of("C_DRAMA_BALANCED");
        List<CandidatePlace> places = List.of(
                place("P_B_01", "C_DRAMA_BALANCED", "Balanced Drama", "Seoul", "Seoul Gangnam-gu Spot 1", 37.5000, 127.0000),
                place("P_B_02", "C_DRAMA_BALANCED", "Balanced Drama", "Seoul", "Seoul Gangnam-gu Spot 2", 37.5010, 127.0010),
                place("P_B_03", "C_DRAMA_BALANCED", "Balanced Drama", "Seoul", "Seoul Gangnam-gu Spot 3", 37.5020, 127.0020),
                place("P_B_04", "C_DRAMA_BALANCED", "Balanced Drama", "Seoul", "Seoul Gangnam-gu Spot 4", 37.5030, 127.0030),
                place("P_B_05", "C_DRAMA_BALANCED", "Balanced Drama", "Seoul", "Seoul Gangnam-gu Spot 5", 37.5040, 127.0040),
                place("P_B_06", "C_DRAMA_BALANCED", "Balanced Drama", "Seoul", "Seoul Gangnam-gu Spot 6", 37.5050, 127.0050),
                place("P_B_07", "C_DRAMA_BALANCED", "Balanced Drama", "Seoul", "Seoul Gangnam-gu Spot 7", 37.5060, 127.0060),
                place("P_B_08", "C_DRAMA_BALANCED", "Balanced Drama", "Seoul", "Seoul Gangnam-gu Spot 8", 37.5070, 127.0070),
                place("P_B_09", "C_DRAMA_BALANCED", "Balanced Drama", "Seoul", "Seoul Gangnam-gu Spot 9", 37.5080, 127.0080),
                place("P_B_10", "C_DRAMA_BALANCED", "Balanced Drama", "Seoul", "Seoul Gangnam-gu Spot 10", 37.5090, 127.0090),
                place("P_B_11", "C_DRAMA_BALANCED", "Balanced Drama", "Seoul", "Seoul Gangnam-gu Spot 11", 37.5100, 127.0100),
                place("P_B_12", "C_DRAMA_BALANCED", "Balanced Drama", "Seoul", "Seoul Gangnam-gu Spot 12", 37.5110, 127.0110),
                place("P_B_13", "C_DRAMA_BALANCED", "Balanced Drama", "Seoul", "Seoul Gangnam-gu Spot 13", 37.5120, 127.0120)
        );
        HttpServer server = startGmsStub(aiRouteJson("Balanced repaired route", List.of(
                List.of("P_B_01", "P_B_02", "P_B_03", "P_B_04"),
                List.of("P_B_05"),
                List.of("P_B_06", "P_B_07", "P_B_08", "P_B_09", "P_B_10", "P_B_11", "P_B_12"),
                List.of("P_B_13")
        )));

        try {
            AiRouteService service = aiRouteServiceWithGms(server, 7);
            AiRouteRequest request = aiRequest(contentIds, "Seoul", 4, "car", List.of("CAR"), List.of(1, 2, 3, 4));
            when(aiRouteMapper.findCandidatePlaces(eq(contentIds), isNull(), anyInt())).thenReturn(places);

            AiRouteDto route = service.generateRoute(request);

            assertThat(route.days()).hasSize(4);
            assertThat(route.days()).allSatisfy(day -> assertThat(day.places()).hasSizeBetween(2, 7));
            assertThat(route.days().stream().map(day -> day.places().size()).toList()).doesNotContain(1);
            assertThat(route.places()).extracting(RouteStopDto::placeId).doesNotHaveDuplicates();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void routeStopDtoFromRoutePlaceIncludesCategory() {
        RoutePlace place = new RoutePlace();
        place.setRoutePlaceId("rp-001");
        place.setPlaceId("P_001");
        place.setPlaceName("연리단길");
        place.setDayNumber(1);
        place.setVisitOrder(1);
        place.setCategory("거리");
        place.setRecommendedTime("09:30");
        place.setReason("콘텐츠 분위기와 어울리는 거리입니다.");
        place.setTravelTip("골목 동선과 함께 보기 좋습니다.");

        RouteStopDto stop = RouteStopDto.from(place);

        assertThat(stop.category()).isEqualTo("거리");
    }
    private CandidatePlace place(String placeId, String contentId, String contentTitle, String region) {
        CandidatePlace place = new CandidatePlace();
        place.setPlaceId(placeId);
        place.setName("Place " + placeId);
        place.setRegion(region);
        place.setCategory("spot");
        place.setAddress(region + " address " + placeId);
        place.setLatitude(37.5);
        place.setLongitude(127.0);
        place.setRelatedContentId(contentId);
        place.setRelatedContentTitle(contentTitle);
        place.setSceneDescription("scene");
        place.setVerified(true);
        place.setImageUrl("https://example.com/" + placeId + ".jpg");
        return place;
    }

    private CandidatePlace place(String placeId, String contentId, String contentTitle, String region, double latitude, double longitude) {
        CandidatePlace place = place(placeId, contentId, contentTitle, region);
        place.setLatitude(latitude);
        place.setLongitude(longitude);
        return place;
    }

    private CandidatePlace place(String placeId, String contentId, String contentTitle, String region, String address, double latitude, double longitude) {
        CandidatePlace place = place(placeId, contentId, contentTitle, region, latitude, longitude);
        place.setAddress(address);
        return place;
    }

    private AiRouteService aiRouteServiceWithGms(HttpServer server, int maxPlacesPerDay) {
        return new AiRouteService(
                aiRouteMapper,
                new ObjectMapper(),
                "http://127.0.0.1:" + server.getAddress().getPort() + "/",
                "test-gms-key",
                "gpt-test",
                1000,
                1000,
                2,
                3,
                maxPlacesPerDay
        );
    }

    private AiRouteRequest aiRequest(List<String> contentIds, String region, int days, String transport, List<String> transportModes, List<Integer> carDays) {
        return new AiRouteRequest(
                null,
                contentIds,
                region,
                List.of(region),
                days,
                null,
                null,
                null,
                transport,
                transportModes,
                carDays,
                "friends",
                "active",
                List.of("active"),
                null,
                600000,
                "standard",
                null
        );
    }

    private HttpServer startGmsStub(String routeJson) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api.openai.com/v1/chat/completions", exchange -> {
            exchange.getRequestBody().readAllBytes();
            String escaped = routeJson.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
            byte[] response = ("{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"" + escaped + "\"}}]}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        return server;
    }

    private String aiRouteJson(String title, List<List<String>> days) {
        StringBuilder dayJson = new StringBuilder();
        for (int dayIndex = 0; dayIndex < days.size(); dayIndex++) {
            if (!dayJson.isEmpty()) dayJson.append(',');
            List<String> placeIds = days.get(dayIndex);
            StringBuilder placeJson = new StringBuilder();
            for (int placeIndex = 0; placeIndex < placeIds.size(); placeIndex++) {
                if (!placeJson.isEmpty()) placeJson.append(',');
                placeJson.append(String.format(
                        "{\"placeId\":\"%s\",\"visitOrder\":%d,\"recommendedTime\":\"%02d:00\",\"recommendReason\":\"scene\",\"estimatedStayMinutes\":45,\"estimatedTravelMinutes\":20,\"travelTip\":\"nearby\"}",
                        placeIds.get(placeIndex),
                        placeIndex + 1,
                        9 + Math.min(placeIndex, 9)
                ));
            }
            dayJson.append(String.format("{\"dayNumber\":%d,\"estimatedTravelTime\":\"이동 약 90분\",\"places\":[%s]}", dayIndex + 1, placeJson));
        }
        return String.format("{\"title\":\"%s\",\"summary\":\"summary\",\"days\":[%s]}", title, dayJson);
    }

    private Set<String> districts(List<RouteStopDto> places) {
        return places.stream()
                .map(RouteStopDto::address)
                .map(address -> address.split("\\s+")[1])
                .collect(Collectors.toSet());
    }
    private boolean containsAllCities(List<RouteStopDto> places, String... cityNames) {
        Set<String> cities = Set.copyOf(cityTokens(places));
        return cities.containsAll(Set.of(cityNames));
    }

    private List<String> cityTokens(List<RouteStopDto> places) {
        return places.stream()
                .map(RouteStopDto::address)
                .map(address -> address.split("\\s+")[1])
                .distinct()
                .toList();
    }

    private double totalDistanceKm(List<RouteStopDto> places) {
        double total = 0.0;
        for (int index = 1; index < places.size(); index++) {
            total += distanceKm(places.get(index - 1), places.get(index));
        }
        return total;
    }

    private double distanceKm(RouteStopDto from, RouteStopDto to) {
        double earthRadiusKm = 6371.0;
        double lat1 = Math.toRadians(from.latitude());
        double lat2 = Math.toRadians(to.latitude());
        double deltaLat = Math.toRadians(to.latitude() - from.latitude());
        double deltaLng = Math.toRadians(to.longitude() - from.longitude());
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        return earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
