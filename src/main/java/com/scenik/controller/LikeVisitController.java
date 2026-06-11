package com.scenik.controller;

import com.scenik.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<Void>> likeContent(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("contentId") String contentId
    ) {
        likeVisitService.likeContent(authService.requireUser(authorization), contentId);
        return ResponseEntity.ok(ApiResponse.okMessage("콘텐츠 좋아요가 반영되었습니다."));
    }

    @DeleteMapping("/api/contents/{contentId}/likes")
    public ResponseEntity<ApiResponse<Void>> unlikeContent(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("contentId") String contentId
    ) {
        likeVisitService.unlikeContent(authService.requireUser(authorization), contentId);
        return ResponseEntity.ok(ApiResponse.okMessage("콘텐츠 좋아요가 취소되었습니다."));
    }

    @PostMapping("/api/places/{placeId}/likes")
    public ResponseEntity<ApiResponse<Void>> likePlace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId
    ) {
        likeVisitService.likePlace(authService.requireUser(authorization), placeId);
        return ResponseEntity.ok(ApiResponse.okMessage("장소 좋아요가 반영되었습니다."));
    }

    @DeleteMapping("/api/places/{placeId}/likes")
    public ResponseEntity<ApiResponse<Void>> unlikePlace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId
    ) {
        likeVisitService.unlikePlace(authService.requireUser(authorization), placeId);
        return ResponseEntity.ok(ApiResponse.okMessage("장소 좋아요가 취소되었습니다."));
    }

    @GetMapping("/api/users/me/likes")
    public ApiResponse<LikesDto> myLikes(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResponse.ok(likeVisitService.getLikes(authService.requireUser(authorization)));
    }

    @GetMapping("/api/users/me/liked-contents")
    public ResponseEntity<ApiResponse<List<String>>> myLikedContents(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        List<String> contentIds = likeVisitService.getLikedContentIds(authService.requireUser(authorization));
        List<String> paged = ListQuerySupport.paginate(contentIds, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(contentIds.size()))
                .body(ApiResponse.ok(paged));
    }

    @GetMapping("/api/users/me/liked-places")
    public ResponseEntity<ApiResponse<List<String>>> myLikedPlaces(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        List<String> placeIds = likeVisitService.getLikedPlaceIds(authService.requireUser(authorization));
        List<String> paged = ListQuerySupport.paginate(placeIds, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(placeIds.size()))
                .body(ApiResponse.ok(paged));
    }

    @PostMapping("/api/places/{placeId}/visit-status")
    public ApiResponse<VisitStatusDto> setVisitStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId,
            @Valid @RequestBody VisitStatusRequest request
    ) {
        return ApiResponse.ok("방문 상태가 저장되었습니다.",
                likeVisitService.setVisitStatus(authService.requireUser(authorization), placeId, request.status()));
    }

    @PutMapping("/api/places/{placeId}/visit-status")
    public ApiResponse<VisitStatusDto> putVisitStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId,
            @Valid @RequestBody VisitStatusRequest request
    ) {
        return ApiResponse.ok("방문 상태가 저장되었습니다.",
                likeVisitService.setVisitStatus(authService.requireUser(authorization), placeId, request.status()));
    }

    @DeleteMapping("/api/places/{placeId}/visit-status")
    public ResponseEntity<ApiResponse<Void>> deleteVisitStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId
    ) {
        likeVisitService.deleteVisitStatus(authService.requireUser(authorization), placeId);
        return ResponseEntity.ok(ApiResponse.okMessage("방문 상태가 삭제되었습니다."));
    }

    @GetMapping("/api/users/me/visit-status")
    public ResponseEntity<ApiResponse<List<VisitStatusDto>>> myVisitStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        List<VisitStatusDto> statuses = likeVisitService.getVisitStatuses(authService.requireUser(authorization));
        List<VisitStatusDto> paged = ListQuerySupport.paginate(statuses, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(statuses.size()))
                .body(ApiResponse.ok(paged));
    }
}
