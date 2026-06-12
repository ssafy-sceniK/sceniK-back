package com.scenik.controller;

import com.scenik.dto.CommunityDtos.CommentDto;
import com.scenik.dto.CommunityDtos.ImageUploadDto;
import com.scenik.dto.CommunityDtos.CommentRequest;
import com.scenik.dto.CommunityDtos.NotificationDto;
import com.scenik.dto.CommunityDtos.PostDto;
import com.scenik.dto.CommunityDtos.PostRequest;
import com.scenik.dto.CommunityDtos.ReportDto;
import com.scenik.dto.CommunityDtos.ReportRequest;
import com.scenik.domain.User;
import com.scenik.service.AuthService;
import com.scenik.service.CommunityImageStorageService;
import com.scenik.service.CommunityService;
import com.scenik.util.ListQuerySupport;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class CommunityController {
    private final AuthService authService;
    private final CommunityService communityService;
    private final CommunityImageStorageService communityImageStorageService;

    public CommunityController(
            AuthService authService,
            CommunityService communityService,
            CommunityImageStorageService communityImageStorageService
    ) {
        this.authService = authService;
        this.communityService = communityService;
        this.communityImageStorageService = communityImageStorageService;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostDto>> posts(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "region", required = false) String region
    ) {
        ListQuerySupport.validateSort(sort, Set.of("latest", "popular"));
        List<PostDto> posts = communityService.listPosts(authorization).stream()
                .filter(post -> matchesPost(post, keyword, region))
                .sorted(postComparator(ListQuerySupport.normalizeSort(sort)))
                .toList();
        List<PostDto> paged = ListQuerySupport.paginate(posts, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(posts.size()))
                .body(paged);
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

    @PostMapping(value = "/posts/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageUploadDto uploadPostImage(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("file") MultipartFile file
    ) {
        authService.requireUser(authorization);
        return communityImageStorageService.store(file);
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
    public ResponseEntity<List<CommentDto>> comments(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("postId") String postId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        List<CommentDto> comments = communityService.listComments(authorization, postId);
        List<CommentDto> paged = ListQuerySupport.paginate(comments, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(comments.size()))
                .body(paged);
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

    @PostMapping("/posts/comments/{commentId}/likes")
    public CommentDto likeComment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("commentId") String commentId
    ) {
        return communityService.likeComment(authService.requireUser(authorization), commentId);
    }

    @DeleteMapping("/posts/comments/{commentId}/likes")
    public CommentDto unlikeComment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("commentId") String commentId
    ) {
        return communityService.unlikeComment(authService.requireUser(authorization), commentId);
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
    public ResponseEntity<List<NotificationDto>> notifications(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        ListQuerySupport.validateSort(sort, Set.of("latest"));
        User user = authService.requireUser(authorization);
        List<NotificationDto> notifications = communityService.listNotifications(user).stream()
                .sorted(notificationComparator(ListQuerySupport.normalizeSort(sort)))
                .toList();
        List<NotificationDto> paged = ListQuerySupport.paginate(notifications, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(notifications.size()))
                .header("X-Unread-Count", String.valueOf(communityService.countUnreadNotifications(user)))
                .body(paged);
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
    public ResponseEntity<List<PostDto>> myPosts(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        List<PostDto> posts = communityService.listMyPosts(authService.requireUser(authorization));
        List<PostDto> paged = ListQuerySupport.paginate(posts, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(posts.size()))
                .body(paged);
    }

    private boolean matchesPost(PostDto post, String keyword, String region) {
        boolean regionMatch = region == null || region.isBlank() || ListQuerySupport.containsText(post.region(), region);
        boolean keywordMatch = ListQuerySupport.containsText(post.title(), keyword)
                || ListQuerySupport.containsText(post.content(), keyword)
                || ListQuerySupport.containsText(post.body(), keyword)
                || (post.tags() != null && post.tags().stream().anyMatch(tag -> ListQuerySupport.containsText(tag, keyword)));
        return regionMatch && keywordMatch;
    }

    private Comparator<PostDto> postComparator(String sort) {
        Comparator<PostDto> byCreatedDesc = Comparator.comparing(PostDto::createdAt, Comparator.nullsLast(Comparator.reverseOrder()));
        if ("popular".equals(sort)) {
            return Comparator.comparingInt((PostDto post) -> post.likeCount() == null ? 0 : post.likeCount())
                    .reversed()
                    .thenComparing(byCreatedDesc);
        }
        return byCreatedDesc;
    }

    private Comparator<NotificationDto> notificationComparator(String sort) {
        return Comparator.comparing(NotificationDto::createdAt, Comparator.nullsLast(Comparator.reverseOrder()));
    }
}
