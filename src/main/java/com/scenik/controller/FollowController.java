package com.scenik.controller;

import com.scenik.dto.ApiResponse;
import com.scenik.domain.User;
import com.scenik.dto.FollowDtos.FollowStatusDto;
import com.scenik.dto.FollowDtos.FollowUserDto;
import com.scenik.dto.FollowDtos.PublicUserProfileDto;
import com.scenik.service.AuthService;
import com.scenik.service.CommunityService;
import com.scenik.service.FollowService;
import com.scenik.service.RouteService;
import com.scenik.util.ListQuerySupport;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FollowController {
    private final AuthService authService;
    private final FollowService followService;
    private final RouteService routeService;
    private final CommunityService communityService;

    public FollowController(AuthService authService, FollowService followService, RouteService routeService, CommunityService communityService) {
        this.authService = authService;
        this.followService = followService;
        this.routeService = routeService;
        this.communityService = communityService;
    }

    @PostMapping("/api/users/{userId}/follow")
    public ApiResponse<FollowStatusDto> follow(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("userId") Long userId
    ) {
        return ApiResponse.ok("팔로우했습니다.", followService.follow(authService.requireUser(authorization), userId));
    }

    @DeleteMapping("/api/users/{userId}/follow")
    public ApiResponse<FollowStatusDto> unfollow(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("userId") Long userId
    ) {
        return ApiResponse.ok("팔로우를 취소했습니다.", followService.unfollow(authService.requireUser(authorization), userId));
    }

    @GetMapping("/api/users/{userId}/profile")
    public ApiResponse<PublicUserProfileDto> profile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("userId") Long userId
    ) {
        User actor = authorization == null || authorization.isBlank() ? null : authService.requireUser(authorization);
        return ApiResponse.ok(followService.profile(
                actor,
                userId,
                routeService.listRoutesByUserId(userId),
                communityService.listPostsByAuthor(actor, userId)
        ));
    }

    @GetMapping("/api/users/{userId}/followers")
    public ResponseEntity<ApiResponse<List<FollowUserDto>>> followers(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        List<FollowUserDto> followers = followService.followers(authService.requireUser(authorization), userId);
        List<FollowUserDto> paged = ListQuerySupport.paginate(followers, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(followers.size()))
                .body(ApiResponse.ok(paged));
    }

    @GetMapping("/api/users/{userId}/following")
    public ResponseEntity<ApiResponse<List<FollowUserDto>>> following(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        List<FollowUserDto> following = followService.following(authService.requireUser(authorization), userId);
        List<FollowUserDto> paged = ListQuerySupport.paginate(following, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(following.size()))
                .body(ApiResponse.ok(paged));
    }

    @GetMapping("/api/users/me/following")
    public ResponseEntity<ApiResponse<List<FollowUserDto>>> myFollowing(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        var user = authService.requireUser(authorization);
        List<FollowUserDto> following = followService.following(user, user.getUserId());
        List<FollowUserDto> paged = ListQuerySupport.paginate(following, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(following.size()))
                .body(ApiResponse.ok(paged));
    }
}
