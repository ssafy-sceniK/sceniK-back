package com.scenik.controller;

import com.scenik.dto.RouteDtos.AiRouteDto;
import com.scenik.dto.RouteDtos.RoutePlaceReorderRequest;
import com.scenik.dto.RouteDtos.RoutePlaceRequest;
import com.scenik.service.AuthService;
import com.scenik.service.RouteService;
import com.scenik.util.ListQuerySupport;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RouteController {
    private final AuthService authService;
    private final RouteService routeService;

    public RouteController(AuthService authService, RouteService routeService) {
        this.authService = authService;
        this.routeService = routeService;
    }

    @GetMapping("/api/users/me/routes")
    public ResponseEntity<List<AiRouteDto>> myRoutes(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        ListQuerySupport.validateSort(sort, Set.of("latest", "updated"));
        List<AiRouteDto> routes = routeService.listMyRoutes(authService.requireUser(authorization)).stream()
                .sorted(routeComparator(ListQuerySupport.normalizeSort(sort)))
                .toList();
        List<AiRouteDto> paged = ListQuerySupport.paginate(routes, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(routes.size()))
                .body(paged);
    }

    @PostMapping("/api/routes")
    public AiRouteDto create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody AiRouteDto request
    ) {
        return routeService.createRoute(authService.requireUser(authorization), request);
    }

    @GetMapping("/api/routes/{routeId}")
    public AiRouteDto detail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("routeId") String routeId
    ) {
        return routeService.getRoute(authService.requireUser(authorization), routeId);
    }

    @PatchMapping("/api/routes/{routeId}")
    public AiRouteDto update(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("routeId") String routeId,
            @Valid @RequestBody AiRouteDto request
    ) {
        return routeService.updateRoute(authService.requireUser(authorization), routeId, request);
    }

    @DeleteMapping("/api/routes/{routeId}")
    public ResponseEntity<Void> delete(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("routeId") String routeId
    ) {
        routeService.deleteRoute(authService.requireUser(authorization), routeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/routes/{routeId}/places")
    public AiRouteDto addPlace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("routeId") String routeId,
            @Valid @RequestBody RoutePlaceRequest request
    ) {
        return routeService.addPlace(authService.requireUser(authorization), routeId, request);
    }

    @PatchMapping("/api/routes/{routeId}/places/{routePlaceId}")
    public AiRouteDto updatePlace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("routeId") String routeId,
            @PathVariable("routePlaceId") String routePlaceId,
            @Valid @RequestBody RoutePlaceRequest request
    ) {
        return routeService.updatePlace(authService.requireUser(authorization), routeId, routePlaceId, request);
    }

    @DeleteMapping("/api/routes/{routeId}/places/{routePlaceId}")
    public AiRouteDto deletePlace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("routeId") String routeId,
            @PathVariable("routePlaceId") String routePlaceId
    ) {
        return routeService.deletePlace(authService.requireUser(authorization), routeId, routePlaceId);
    }

    @PatchMapping("/api/routes/{routeId}/places/reorder")
    public AiRouteDto reorderPlaces(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("routeId") String routeId,
            @Valid @RequestBody RoutePlaceReorderRequest request
    ) {
        return routeService.reorderPlaces(authService.requireUser(authorization), routeId, request);
    }

    private Comparator<AiRouteDto> routeComparator(String sort) {
        if ("updated".equals(sort)) {
            return Comparator.comparing(AiRouteDto::updatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
        }
        return Comparator.comparing(AiRouteDto::createdAt, Comparator.nullsLast(Comparator.reverseOrder()));
    }
}
