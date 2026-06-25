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
import com.scenik.dto.RouteDtos.RouteStopDto;
import com.scenik.mapper.AiRouteMapper;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
                place("P_B", "C_DRAMA_030", "Drama", "Seoul", 37.01, 129.00),
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

    private Set<String> districts(List<RouteStopDto> places) {
        return places.stream()
                .map(RouteStopDto::address)
                .map(address -> address.split("\\s+")[1])
                .collect(Collectors.toSet());
    }
}
