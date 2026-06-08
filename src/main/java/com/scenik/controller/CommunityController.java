package com.scenik.controller;

import com.scenik.dto.CommunityDtos.CommentDto;
import com.scenik.dto.CommunityDtos.CommentRequest;
import com.scenik.dto.CommunityDtos.NotificationDto;
import com.scenik.dto.CommunityDtos.PostDto;
import com.scenik.dto.CommunityDtos.PostRequest;
import com.scenik.dto.CommunityDtos.ReportDto;
import com.scenik.dto.CommunityDtos.ReportRequest;
import com.scenik.domain.User;
import com.scenik.service.AuthService;
import com.scenik.service.CommunityService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CommunityController {
    private final AuthService authService;
    private final CommunityService communityService;

    public CommunityController(AuthService authService, CommunityService communityService) {
        this.authService = authService;
        this.communityService = communityService;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostDto>> posts(@RequestHeader(value = "Authorization", required = false) String authorization) {
        List<PostDto> posts = communityService.listPosts(authorization);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(posts.size()))
                .body(posts);
    }

    @GetMapping("/posts/{postId}")
    public PostDto post(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("postId") String postId
    ) {
        return communityService.getPost(authorization, postId);
    }

    @PostMapping("/posts")
    public PostDto createPost(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody PostRequest request
    ) {
        return communityService.createPost(authService.requireUser(authorization), request);
    }

    @PatchMapping("/posts/{postId}")
    public PostDto updatePost(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("postId") String postId,
            @Valid @RequestBody PostRequest request
    ) {
        return communityService.updatePost(authService.requireUser(authorization), postId, request);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("postId") String postId
    ) {
        communityService.deletePost(authService.requireUser(authorization), postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentDto>> comments(@PathVariable("postId") String postId) {
        List<CommentDto> comments = communityService.listComments(postId);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(comments.size()))
                .body(comments);
    }

    @PostMapping("/posts/{postId}/comments")
    public CommentDto createComment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("postId") String postId,
            @Valid @RequestBody CommentRequest request
    ) {
        return communityService.createComment(authService.requireUser(authorization), postId, request);
    }

    @PatchMapping("/posts/{postId}/comments/{commentId}")
    public CommentDto updateComment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("postId") String postId,
            @PathVariable("commentId") String commentId,
            @Valid @RequestBody CommentRequest request
    ) {
        return communityService.updateComment(authService.requireUser(authorization), postId, commentId, request);
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("postId") String postId,
            @PathVariable("commentId") String commentId
    ) {
        communityService.deleteComment(authService.requireUser(authorization), postId, commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/likes")
    public PostDto likePost(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("postId") String postId
    ) {
        return communityService.likePost(authService.requireUser(authorization), postId);
    }

    @DeleteMapping("/posts/{postId}/likes")
    public PostDto unlikePost(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("postId") String postId
    ) {
        return communityService.unlikePost(authService.requireUser(authorization), postId);
    }

    @PostMapping("/posts/{postId}/reports")
    public ReportDto reportPost(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("postId") String postId,
            @Valid @RequestBody(required = false) ReportRequest request
    ) {
        return communityService.reportPost(authService.requireUser(authorization), postId, request);
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDto>> notifications(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User user = authService.requireUser(authorization);
        List<NotificationDto> notifications = communityService.listNotifications(user);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(notifications.size()))
                .header("X-Unread-Count", String.valueOf(communityService.countUnreadNotifications(user)))
                .body(notifications);
    }

    @PatchMapping("/notifications/read")
    public Map<String, Object> markAllNotificationsRead(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return communityService.markAllNotificationsRead(authService.requireUser(authorization));
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public NotificationDto markNotificationRead(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("notificationId") String notificationId
    ) {
        return communityService.markNotificationRead(authService.requireUser(authorization), notificationId);
    }

    @GetMapping("/users/me/posts")
    public ResponseEntity<List<PostDto>> myPosts(@RequestHeader(value = "Authorization", required = false) String authorization) {
        List<PostDto> posts = communityService.listMyPosts(authService.requireUser(authorization));
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(posts.size()))
                .body(posts);
    }
}
