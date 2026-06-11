package com.scenik.controller;

import com.scenik.dto.ApiResponse;
import com.scenik.dto.FollowDtos.FollowStatusDto;
import com.scenik.dto.FollowDtos.FollowUserDto;
import com.scenik.service.AuthService;
import com.scenik.service.FollowService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FollowController {
    private final AuthService authService;
    private final FollowService followService;

    public FollowController(AuthService authService, FollowService followService) {
        this.authService = authService;
        this.followService = followService;
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

    @GetMapping("/api/users/{userId}/followers")
    public ApiResponse<List<FollowUserDto>> followers(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("userId") Long userId
    ) {
        return ApiResponse.ok(followService.followers(authService.requireUser(authorization), userId));
    }

    @GetMapping("/api/users/{userId}/following")
    public ApiResponse<List<FollowUserDto>> following(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("userId") Long userId
    ) {
        return ApiResponse.ok(followService.following(authService.requireUser(authorization), userId));
    }

    @GetMapping("/api/users/me/following")
    public ApiResponse<List<FollowUserDto>> myFollowing(@RequestHeader(value = "Authorization", required = false) String authorization) {
        var user = authService.requireUser(authorization);
        return ApiResponse.ok(followService.following(user, user.getUserId()));
    }
}
