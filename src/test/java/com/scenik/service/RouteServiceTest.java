package com.scenik.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scenik.domain.Place;
import com.scenik.domain.RoutePlace;
import com.scenik.domain.TravelRoute;
import com.scenik.domain.User;
import com.scenik.dto.RouteDtos.AiRouteDto;
import com.scenik.mapper.CommunityMapper;
import com.scenik.mapper.PlaceMapper;
import com.scenik.mapper.RouteMapper;
import com.scenik.mapper.RoutePlaceMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {
    @Mock RouteMapper routeMapper;
    @Mock RoutePlaceMapper routePlaceMapper;
    @Mock PlaceMapper placeMapper;
    @Mock CommunityMapper communityMapper;

    private RouteService service;

    @BeforeEach
    void setUp() {
        service = new RouteService(routeMapper, routePlaceMapper, placeMapper, communityMapper);
    }

    @Test
    void nonOwnerLoggedInUserCanViewSharedCourseAsReadOnly() {
        TravelRoute route = route("route-source", 1L);
        when(routeMapper.findById("route-source")).thenReturn(route);
        when(routePlaceMapper.findByRouteId("route-source")).thenReturn(List.of(routePlace("rp-source", "route-source")));

        AiRouteDto detail = service.getRoute(2L, "route-source");

        assertThat(detail.routeId()).isEqualTo("route-source");
        assertThat(detail.ownerUserId()).isEqualTo(1L);
        assertThat(detail.isOwner()).isFalse();
        assertThat(detail.canEdit()).isFalse();
        assertThat(detail.canDelete()).isFalse();
        assertThat(detail.canCopy()).isTrue();
    }

    @Test
    void nonOwnerCannotUpdateOriginalCourse() {
        when(routeMapper.findById("route-source")).thenReturn(route("route-source", 1L));

        assertThatThrownBy(() -> service.updateRoute(user(2L), "route-source", null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void copyRouteCreatesOwnedCopyWithoutMutatingOriginal() {
        TravelRoute source = route("route-source", 1L);
        RoutePlace sourcePlace = routePlace("rp-source", "route-source");
        User copier = user(2L);
        when(routeMapper.findById("route-source")).thenReturn(source);
        when(routePlaceMapper.findByRouteId("route-source")).thenReturn(List.of(sourcePlace));
        when(placeMapper.findByIdForWrite("P_001")).thenReturn(place("P_001"));

        AiRouteDto copied = service.copyRoute(copier, "route-source");

        ArgumentCaptor<TravelRoute> routeCaptor = ArgumentCaptor.forClass(TravelRoute.class);
        ArgumentCaptor<RoutePlace> placeCaptor = ArgumentCaptor.forClass(RoutePlace.class);
        verify(routeMapper).insert(routeCaptor.capture());
        verify(routePlaceMapper).insert(placeCaptor.capture());
        verify(routeMapper, never()).update(any());

        TravelRoute insertedRoute = routeCaptor.getValue();
        RoutePlace insertedPlace = placeCaptor.getValue();
        assertThat(insertedRoute.getUserId()).isEqualTo(2L);
        assertThat(insertedRoute.getRouteId()).isNotEqualTo("route-source");
        assertThat(insertedRoute.getTitle()).isEqualTo("Original course");
        assertThat(insertedPlace.getRouteId()).isEqualTo(insertedRoute.getRouteId());
        assertThat(insertedPlace.getRoutePlaceId()).isNotEqualTo("rp-source");
        assertThat(insertedPlace.getPlaceId()).isEqualTo("P_001");
        assertThat(copied.routeId()).isEqualTo(insertedRoute.getRouteId());
        assertThat(copied.ownerUserId()).isEqualTo(2L);
        assertThat(copied.isOwner()).isTrue();
        assertThat(copied.canEdit()).isTrue();
        assertThat(copied.canDelete()).isTrue();
        assertThat(copied.canCopy()).isFalse();
    }

    private TravelRoute route(String routeId, Long userId) {
        TravelRoute route = new TravelRoute();
        route.setRouteId(routeId);
        route.setUserId(userId);
        route.setTitle("Original course");
        route.setSummary("Original summary");
        route.setContentId("C_001");
        route.setContentIds("C_001");
        route.setContentTitle("Content");
        route.setRegion("Seoul");
        route.setDurationDays(1);
        route.setRequestedDurationDays(1);
        route.setTransport("PUBLIC_TRANSIT");
        route.setCompanion("friends");
        route.setStyle("감성 여행");
        route.setBudgetMin(100000);
        route.setBudgetMax(200000);
        route.setBudget("10만원 ~ 20만원");
        route.setTrustMessage("Verified places");
        return route;
    }

    private RoutePlace routePlace(String routePlaceId, String routeId) {
        RoutePlace place = new RoutePlace();
        place.setRoutePlaceId(routePlaceId);
        place.setRouteId(routeId);
        place.setPlaceId("P_001");
        place.setDayNumber(1);
        place.setVisitOrder(1);
        place.setRecommendedTime("10:00");
        place.setReason("Route reason");
        place.setTravelTip("Travel tip");
        place.setPlaceName("Place");
        place.setRegion("Seoul");
        place.setCategory("촬영지");
        place.setAddress("Seoul address");
        place.setLatitude(37.5);
        place.setLongitude(127.0);
        place.setImageUrl("/image.jpg");
        place.setRelatedContentId("C_001");
        place.setRelatedContentTitle("Content");
        place.setSceneDescription("Scene");
        return place;
    }

    private Place place(String placeId) {
        Place place = new Place();
        place.setPlaceId(placeId);
        place.setPlaceName("Place");
        place.setStatus("ACTIVE");
        return place;
    }

    private User user(Long userId) {
        User user = new User();
        user.setUserId(userId);
        user.setStatus("ACTIVE");
        return user;
    }
}
