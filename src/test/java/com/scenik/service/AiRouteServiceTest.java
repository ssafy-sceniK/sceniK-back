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
                5
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
}
