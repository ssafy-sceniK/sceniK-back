package com.scenik.controller;

import com.scenik.dto.RouteDtos.AiRouteDto;
import com.scenik.dto.RouteDtos.AiRouteRequest;
import com.scenik.service.AiRouteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/routes")
public class AiRouteController {
    private final AiRouteService aiRouteService;

    public AiRouteController(AiRouteService aiRouteService) {
        this.aiRouteService = aiRouteService;
    }

    @PostMapping
    public AiRouteDto generate(@Valid @RequestBody AiRouteRequest request) {
        return aiRouteService.generateRoute(request);
    }

    @PostMapping("/{generationId}/regenerate")
    public AiRouteDto regenerate(@PathVariable("generationId") String generationId) {
        return aiRouteService.regenerateRoute(generationId);
    }
}
