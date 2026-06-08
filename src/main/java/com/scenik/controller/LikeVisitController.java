package com.scenik.controller;

import com.scenik.dto.LikeVisitDtos.LikesDto;
import com.scenik.dto.LikeVisitDtos.VisitStatusDto;
import com.scenik.dto.LikeVisitDtos.VisitStatusRequest;
import com.scenik.service.AuthService;
import com.scenik.service.LikeVisitService;
import com.scenik.util.ListQuerySupport;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LikeVisitController {
    private final AuthService authService;
    private final LikeVisitService likeVisitService;

    public LikeVisitController(AuthService authService, LikeVisitService likeVisitService) {
        this.authService = authService;
        this.likeVisitService = likeVisitService;
    }

    @PostMapping("/api/contents/{contentId}/likes")
    public ResponseEntity<Void> likeContent(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("contentId") String contentId
    ) {
        likeVisitService.likeContent(authService.requireUser(authorization), contentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/contents/{contentId}/likes")
    public ResponseEntity<Void> unlikeContent(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("contentId") String contentId
    ) {
        likeVisitService.unlikeContent(authService.requireUser(authorization), contentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/places/{placeId}/likes")
    public ResponseEntity<Void> likePlace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId
    ) {
        likeVisitService.likePlace(authService.requireUser(authorization), placeId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/places/{placeId}/likes")
    public ResponseEntity<Void> unlikePlace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId
    ) {
        likeVisitService.unlikePlace(authService.requireUser(authorization), placeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/users/me/likes")
    public LikesDto myLikes(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return likeVisitService.getLikes(authService.requireUser(authorization));
    }

    @GetMapping("/api/users/me/liked-contents")
    public ResponseEntity<List<String>> myLikedContents(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        List<String> contentIds = likeVisitService.getLikedContentIds(authService.requireUser(authorization));
        List<String> paged = ListQuerySupport.paginate(contentIds, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(contentIds.size()))
                .body(paged);
    }

    @GetMapping("/api/users/me/liked-places")
    public ResponseEntity<List<String>> myLikedPlaces(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        List<String> placeIds = likeVisitService.getLikedPlaceIds(authService.requireUser(authorization));
        List<String> paged = ListQuerySupport.paginate(placeIds, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(placeIds.size()))
                .body(paged);
    }

    @PostMapping("/api/places/{placeId}/visit-status")
    public VisitStatusDto setVisitStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId,
            @Valid @RequestBody VisitStatusRequest request
    ) {
        return likeVisitService.setVisitStatus(authService.requireUser(authorization), placeId, request.status());
    }

    @PutMapping("/api/places/{placeId}/visit-status")
    public VisitStatusDto putVisitStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId,
            @Valid @RequestBody VisitStatusRequest request
    ) {
        return likeVisitService.setVisitStatus(authService.requireUser(authorization), placeId, request.status());
    }

    @DeleteMapping("/api/places/{placeId}/visit-status")
    public ResponseEntity<Void> deleteVisitStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId
    ) {
        likeVisitService.deleteVisitStatus(authService.requireUser(authorization), placeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/users/me/visit-status")
    public ResponseEntity<List<VisitStatusDto>> myVisitStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        List<VisitStatusDto> statuses = likeVisitService.getVisitStatuses(authService.requireUser(authorization));
        List<VisitStatusDto> paged = ListQuerySupport.paginate(statuses, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(statuses.size()))
                .body(paged);
    }
}
