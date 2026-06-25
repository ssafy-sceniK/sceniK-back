package com.scenik.service;

import com.scenik.domain.CommunityComment;
import com.scenik.domain.CommunityPost;
import com.scenik.domain.CommunityPostImage;
import com.scenik.domain.Notification;
import com.scenik.domain.TravelRoute;
import com.scenik.domain.User;
import com.scenik.dto.CommunityDtos.CommentDto;
import com.scenik.dto.CommunityDtos.CommentRequest;
import com.scenik.dto.CommunityDtos.NotificationDto;
import com.scenik.dto.CommunityDtos.PostAttachmentDto;
import com.scenik.dto.CommunityDtos.PostDto;
import com.scenik.dto.CommunityDtos.PostRequest;
import com.scenik.dto.CommunityDtos.ReportDto;
import com.scenik.dto.CommunityDtos.ReportRequest;
import com.scenik.mapper.CommunityMapper;
import com.scenik.mapper.RouteMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityService {
    private final AuthService authService;
    private final CommunityMapper communityMapper;
    private final RouteMapper routeMapper;
    private final LanguageDetectionService languageDetectionService;

    public CommunityService(AuthService authService, CommunityMapper communityMapper, RouteMapper routeMapper, LanguageDetectionService languageDetectionService) {
        this.authService = authService;
        this.communityMapper = communityMapper;
        this.routeMapper = routeMapper;
        this.languageDetectionService = languageDetectionService;
    }

    public List<PostDto> listPosts(String authorization) {
        Long userId = authService.optionalUserId(authorization);
        return communityMapper.findPosts(userId).stream().map(post -> toPostDto(post, false, userId)).toList();
    }

    public List<PostDto> listMyPosts(User user) {
        return listPostsByAuthor(user, user.getUserId());
    }

    public List<PostDto> listPostsByAuthor(User viewer, Long authorId) {
        Long viewerUserId = viewer == null ? null : viewer.getUserId();
        return communityMapper.findPostsByAuthorId(authorId, viewerUserId).stream()
                .map(post -> toPostDto(post, false, viewerUserId))
                .toList();
    }

    public PostDto getPost(String authorization, String postId) {
        Long userId = authService.optionalUserId(authorization);
        return toPostDto(requirePost(postId, userId), true, userId);
    }

    @Transactional
    public PostDto createPost(User user, PostRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post request is required.");
        }
        List<PostAttachmentDto> attachments = normalizedAttachments(request.attachments(), request.imageUrl());
        CommunityPost post = new CommunityPost();
        post.setPostId("post-" + UUID.randomUUID());
        post.setUserId(user.getUserId());
        post.setTitle(requireText(request.title(), "title is required."));
        post.setContent(requireText(firstText(request.content(), request.body()), "content is required."));
        post.setLanguage(languageDetectionService.detect(post.getTitle() + " " + post.getContent(), user.getLanguage()));
        post.setRegion(firstText(request.region(), "Korea"));
        post.setRouteId(validatedRouteId(user, request.routeId()));
        post.setImageUrl(primaryImageUrl(attachments));
        post.setStatus("ACTIVE");
        communityMapper.insertPost(post);
        replaceTags(post.getPostId(), request.tags());
        replaceImages(post.getPostId(), attachments);
        return toPostDto(requirePost(post.getPostId(), user.getUserId()), true, user.getUserId());
    }

    @Transactional
    public PostDto updatePost(User user, String postId, PostRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post request is required.");
        }
        CommunityPost existing = requireOwnedPost(user, postId);
        List<PostAttachmentDto> attachments = request.attachments() == null && request.imageUrl() == null
                ? null
                : normalizedAttachments(request.attachments(), request.imageUrl());
        CommunityPost post = new CommunityPost();
        post.setPostId(postId);
        post.setUserId(existing.getUserId());
        post.setTitle(requireText(firstText(request.title(), existing.getTitle()), "title is required."));
        post.setContent(requireText(firstText(firstText(request.content(), request.body()), existing.getContent()), "content is required."));
        post.setLanguage(languageDetectionService.detect(post.getTitle() + " " + post.getContent(), user.getLanguage()));
        post.setRegion(firstText(request.region(), existing.getRegion()));
        post.setRouteId(request.routeId() == null ? existing.getRouteId() : validatedRouteId(user, request.routeId()));
        post.setImageUrl(attachments == null ? existing.getImageUrl() : primaryImageUrl(attachments));
        post.setStatus(firstText(existing.getStatus(), "ACTIVE"));
        communityMapper.updatePost(post);
        if (request.tags() != null) {
            replaceTags(postId, request.tags());
        }
        if (attachments != null) {
            replaceImages(postId, attachments);
        }
        return toPostDto(requirePost(postId, user.getUserId()), true, user.getUserId());
    }

    @Transactional
    public void deletePost(User user, String postId) {
        requireOwnedPost(user, postId);
        communityMapper.softDeletePost(postId);
    }

    public List<CommentDto> listComments(String authorization, String postId) {
        Long userId = authService.optionalUserId(authorization);
        requirePost(postId, userId);
        return communityMapper.findCommentsByPostId(postId, userId).stream().map(this::toCommentDto).toList();
    }

    @Transactional
    public CommentDto createComment(User user, String postId, CommentRequest request) {
        CommunityPost post = requirePost(postId, user.getUserId());
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment request is required.");
        }
        String parentCommentId = trimToNull(request.parentCommentId());
        CommunityComment parentComment = null;
        if (parentCommentId != null) {
            parentComment = requireComment(postId, parentCommentId, user.getUserId());
            if (parentComment.getParentCommentId() != null && !parentComment.getParentCommentId().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nested replies are not supported.");
            }
        }
        CommunityComment comment = new CommunityComment();
        comment.setCommentId("comment-" + UUID.randomUUID());
        comment.setPostId(postId);
        comment.setUserId(user.getUserId());
        comment.setParentCommentId(parentCommentId);
        comment.setContent(requireText(firstText(request.content(), request.text()), "content is required."));
        comment.setLanguage(languageDetectionService.detect(comment.getContent(), user.getLanguage()));
        comment.setStatus("ACTIVE");
        communityMapper.insertComment(comment);
        if (parentComment == null) {
            notifyPostOwner(post, user, "\uC0C8 \uB313\uAE00", actorName(user) + "\uB2D8\uC774 \uAC8C\uC2DC\uAE00\uC5D0 \uB313\uAE00\uC744 \uB0A8\uACBC\uC2B5\uB2C8\uB2E4.", "POST_COMMENT", postId, comment.getCommentId());
        } else {
            notifyParentCommentOwner(post, parentComment, user, comment.getCommentId());
            if (post.getUserId() == null || !post.getUserId().equals(parentComment.getUserId())) {
                notifyPostOwner(post, user, "\uC0C8 \uB313\uAE00", actorName(user) + "\uB2D8\uC774 \uAC8C\uC2DC\uAE00\uC5D0 \uB313\uAE00\uC744 \uB0A8\uACBC\uC2B5\uB2C8\uB2E4.", "POST_COMMENT", postId, comment.getCommentId());
            }
        }
        return toCommentDto(requireComment(postId, comment.getCommentId(), user.getUserId()));
    }

    @Transactional
    public CommentDto updateComment(User user, String postId, String commentId, CommentRequest request) {
        CommunityComment existing = requireOwnedComment(user, postId, commentId);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment request is required.");
        }
        existing.setContent(requireText(firstText(request.content(), request.text()), "content is required."));
        existing.setLanguage(languageDetectionService.detect(existing.getContent(), user.getLanguage()));
        communityMapper.updateComment(existing);
        return toCommentDto(requireComment(postId, commentId, user.getUserId()));
    }

    @Transactional
    public void deleteComment(User user, String postId, String commentId) {
        requireOwnedComment(user, postId, commentId);
        communityMapper.softDeleteComment(commentId);
        communityMapper.deleteNotificationsByCommentThread(postId, commentId);
    }

    @Transactional
    public CommentDto likeComment(User user, String commentId) {
        CommunityComment comment = requireCommentById(commentId, user.getUserId());
        communityMapper.insertCommentLike(user.getUserId(), commentId);
        return toCommentDto(requireComment(comment.getPostId(), commentId, user.getUserId()));
    }

    @Transactional
    public CommentDto unlikeComment(User user, String commentId) {
        CommunityComment comment = requireCommentById(commentId, user.getUserId());
        communityMapper.deleteCommentLike(user.getUserId(), commentId);
        return toCommentDto(requireComment(comment.getPostId(), commentId, user.getUserId()));
    }

    @Transactional
    public PostDto likePost(User user, String postId) {
        CommunityPost post = requirePost(postId, user.getUserId());
        int inserted = communityMapper.insertPostLike(user.getUserId(), postId);
        if (inserted > 0) {
            notifyPostOwner(post, user, "\uC0C8 \uC88B\uC544\uC694", actorName(user) + "\uB2D8\uC774 \uAC8C\uC2DC\uAE00\uC744 \uC88B\uC544\uC694\uD588\uC2B5\uB2C8\uB2E4.", "POST_LIKE", postId, null);
        }
        return toPostDto(requirePost(postId, user.getUserId()), true, user.getUserId());
    }

    @Transactional
    public PostDto unlikePost(User user, String postId) {
        CommunityPost post = requirePost(postId, user.getUserId());
        int deleted = communityMapper.deletePostLike(user.getUserId(), postId);
        if (deleted > 0 && post.getUserId() != null && !post.getUserId().equals(user.getUserId())) {
            communityMapper.deletePostLikeNotification(user.getUserId(), post.getUserId(), postId);
        }
        return toPostDto(requirePost(postId, user.getUserId()), true, user.getUserId());
    }

    @Transactional
    public PostDto scrapPost(User user, String postId) {
        requirePost(postId, user.getUserId());
        communityMapper.insertPostScrap(user.getUserId(), postId);
        return toPostDto(requirePost(postId, user.getUserId()), true, user.getUserId());
    }

    @Transactional
    public PostDto unscrapPost(User user, String postId) {
        requirePost(postId, user.getUserId());
        communityMapper.deletePostScrap(user.getUserId(), postId);
        return toPostDto(requirePost(postId, user.getUserId()), true, user.getUserId());
    }

    @Transactional
    public ReportDto reportPost(User user, String postId, ReportRequest request) {
        requirePost(postId, user.getUserId());
        if (communityMapper.existsReport(user.getUserId(), postId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Post was already reported.");
        }
        String reportId = "report-" + UUID.randomUUID();
        String reason = requireText(request == null ? null : request.reason(), "Report reason is required.");
        communityMapper.insertReport(reportId, user.getUserId(), postId, reason);
        return new ReportDto(
                reportId,
                user.getUserId(),
                "POST",
                postId,
                postId,
                reason,
                "PENDING",
                LocalDateTime.now()
        );
    }

    public List<NotificationDto> listNotifications(User user) {
        return communityMapper.findNotificationsByUserId(user.getUserId()).stream()
                .map(this::toNotificationDto)
                .toList();
    }

    @Transactional
    public NotificationDto markNotificationRead(User user, String notificationId) {
        communityMapper.markNotificationRead(user.getUserId(), notificationId);
        Notification notification = communityMapper.findNotificationById(user.getUserId(), notificationId);
        if (notification == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification was not found.");
        }
        return toNotificationDto(notification);
    }

    @Transactional
    public Map<String, Object> markAllNotificationsRead(User user) {
        int unreadBefore = countUnreadNotifications(user);
        communityMapper.markAllNotificationsRead(user.getUserId());
        int unreadAfter = countUnreadNotifications(user);
        return Map.of(
                "updatedCount", unreadBefore,
                "unreadNotificationCount", unreadAfter,
                "hasUnreadNotifications", unreadAfter > 0
        );
    }

    public int countNotifications(User user) {
        return communityMapper.countNotificationsByUserId(user.getUserId());
    }

    public int countUnreadNotifications(User user) {
        return communityMapper.countUnreadNotificationsByUserId(user.getUserId());
    }

    private CommunityPost requirePost(String postId, Long userId) {
        CommunityPost post = communityMapper.findPostById(postId, userId);
        if (post == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post was not found.");
        }
        return post;
    }

    private CommunityPost requireOwnedPost(User user, String postId) {
        CommunityPost post = requirePost(postId, user.getUserId());
        if (!post.getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Post owner does not match.");
        }
        return post;
    }

    private CommunityComment requireCommentById(String commentId, Long userId) {
        CommunityComment comment = communityMapper.findCommentById(commentId, userId);
        if (comment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment was not found.");
        }
        return comment;
    }

    private CommunityComment requireComment(String postId, String commentId, Long userId) {
        CommunityComment comment = requireCommentById(commentId, userId);
        if (comment == null || !postId.equals(comment.getPostId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment was not found.");
        }
        return comment;
    }

    private CommunityComment requireOwnedComment(User user, String postId, String commentId) {
        CommunityComment comment = requireComment(postId, commentId, user.getUserId());
        if (!comment.getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Comment owner does not match.");
        }
        return comment;
    }

    private void notifyPostOwner(CommunityPost post, User actor, String title, String message, String targetType, String postId, String commentId) {
        if (post.getUserId() == null || post.getUserId().equals(actor.getUserId())) {
            return;
        }
        communityMapper.insertNotification(
                "notification-" + UUID.randomUUID(),
                post.getUserId(),
                title,
                message,
                targetType,
                postId,
                postId,
                commentId,
                actor.getUserId(),
                post.getRouteId(),
                null
        );
    }

    private void notifyParentCommentOwner(CommunityPost post, CommunityComment parentComment, User actor, String commentId) {
        if (parentComment == null || parentComment.getUserId() == null) {
            return;
        }
        if (parentComment.getUserId().equals(actor.getUserId())) {
            return;
        }
        communityMapper.insertNotification(
                "notification-" + UUID.randomUUID(),
                parentComment.getUserId(),
                "\uC0C8 \uB2F5\uAE00",
                actorName(actor) + "\uB2D8\uC774 \uB313\uAE00\uC5D0 \uB2F5\uAE00\uC744 \uB0A8\uACBC\uC2B5\uB2C8\uB2E4.",
                "COMMENT_REPLY",
                post.getPostId(),
                post.getPostId(),
                commentId,
                actor.getUserId(),
                post.getRouteId(),
                null
        );
    }

    private String validatedRouteId(User user, String routeId) {
        String normalized = trimToNull(routeId);
        if (normalized == null) {
            return null;
        }
        TravelRoute route = routeMapper.findById(normalized);
        if (route == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Route was not found.");
        }
        if (!route.getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Route owner does not match.");
        }
        return normalized;
    }

    private void replaceTags(String postId, List<String> tags) {
        communityMapper.deleteTagsByPostId(postId);
        if (tags == null) {
            return;
        }
        tags.stream()
                .map(this::trimToNull)
                .filter(tag -> tag != null)
                .distinct()
                .limit(10)
                .forEach(tag -> communityMapper.insertTag(postId, tag));
    }

    private List<PostAttachmentDto> normalizedAttachments(List<PostAttachmentDto> attachments, String imageUrl) {
        LinkedHashSet<String> seenUrls = new LinkedHashSet<>();
        List<PostAttachmentDto> normalized = new ArrayList<>();
        if (attachments != null) {
            for (PostAttachmentDto attachment : attachments) {
                if (attachment == null) continue;
                addAttachment(normalized, seenUrls, firstText(firstText(attachment.imageUrl(), attachment.url()), attachment.thumbnailUrl()), attachment.name());
            }
        }
        addAttachment(normalized, seenUrls, imageUrl, "이미지 URL");
        return normalized.stream().limit(10).toList();
    }

    private void addAttachment(List<PostAttachmentDto> attachments, LinkedHashSet<String> seenUrls, String imageUrl, String name) {
        String normalizedUrl = trimToNull(imageUrl);
        if (normalizedUrl == null || seenUrls.contains(normalizedUrl)) {
            return;
        }
        seenUrls.add(normalizedUrl);
        attachments.add(new PostAttachmentDto(null, null, normalizedUrl, normalizedUrl, normalizedUrl, "image", trimToNull(name)));
    }

    private String primaryImageUrl(List<PostAttachmentDto> attachments) {
        return attachments.isEmpty() ? null : attachments.get(0).imageUrl();
    }

    private void replaceImages(String postId, List<PostAttachmentDto> attachments) {
        communityMapper.deleteImagesByPostId(postId);
        for (int index = 0; index < attachments.size(); index += 1) {
            PostAttachmentDto attachment = attachments.get(index);
            communityMapper.insertPostImage("post-image-" + UUID.randomUUID(), postId, attachment.imageUrl(), attachment.name(), index);
        }
    }

    private PostAttachmentDto toPostAttachmentDto(CommunityPostImage image) {
        return new PostAttachmentDto(
                image.getImageId(),
                image.getImageId(),
                image.getImageUrl(),
                image.getImageUrl(),
                image.getImageUrl(),
                "image",
                image.getFileName()
        );
    }

    private PostDto toPostDto(CommunityPost post, boolean includeComments, Long viewerUserId) {
        List<String> tags = communityMapper.findTagsByPostId(post.getPostId());
        List<PostAttachmentDto> attachments = communityMapper.findImagesByPostId(post.getPostId()).stream()
                .map(this::toPostAttachmentDto)
                .toList();
        List<CommentDto> comments = includeComments
                ? communityMapper.findCommentsByPostId(post.getPostId(), viewerUserId).stream().map(this::toCommentDto).toList()
                : List.of();
        Integer likeCount = post.getLikeCount() == null ? 0 : post.getLikeCount();
        Integer scrapCount = post.getScrapCount() == null ? 0 : post.getScrapCount();
        Boolean scrapped = Boolean.TRUE.equals(post.getScrapped());
        List<Long> scrappedBy = scrapped && viewerUserId != null ? List.of(viewerUserId) : List.of();
        String nickname = firstText(post.getUserNickname(), "sceniK user");
        String authorProfileImageUrl = post.getUserProfileImageUrl();
        return new PostDto(
                post.getPostId(),
                post.getPostId(),
                post.getUserId(),
                nickname,
                nickname,
                nickname,
                authorProfileImageUrl,
                authorProfileImageUrl,
                authorProfileImageUrl,
                post.getTitle(),
                post.getContent(),
                post.getContent(),
                post.getLanguage(),
                post.getRegion(),
                post.getRouteId(),
                post.getImageUrl(),
                attachments,
                tags,
                likeCount,
                likeCount,
                Boolean.TRUE.equals(post.getLiked()),
                scrapCount,
                scrapped,
                scrappedBy,
                post.getCommentCount() == null ? comments.size() : post.getCommentCount(),
                comments,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    private CommentDto toCommentDto(CommunityComment comment) {
        String nickname = firstText(comment.getUserNickname(), "sceniK user");
        String authorProfileImageUrl = comment.getUserProfileImageUrl();
        Integer likeCount = comment.getLikeCount() == null ? 0 : comment.getLikeCount();
        Boolean likedByMe = Boolean.TRUE.equals(comment.getLiked());
        return new CommentDto(
                comment.getCommentId(),
                comment.getCommentId(),
                comment.getPostId(),
                comment.getUserId(),
                nickname,
                nickname,
                nickname,
                authorProfileImageUrl,
                authorProfileImageUrl,
                authorProfileImageUrl,
                comment.getContent(),
                comment.getContent(),
                comment.getLanguage(),
                likeCount,
                likeCount,
                likedByMe,
                likedByMe,
                comment.getParentCommentId(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    private NotificationDto toNotificationDto(Notification notification) {
        boolean read = notification.getReadAt() != null;
        String dateLabel = notification.getCreatedAt() == null
                ? ""
                : notification.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return new NotificationDto(
                notification.getNotificationId(),
                notification.getNotificationId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getMessage(),
                read,
                read,
                notification.getCreatedAt(),
                dateLabel,
                notification.getTargetType(),
                notification.getTargetId(),
                notification.getPostId(),
                notification.getCommentId(),
                notification.getActorUserId(),
                notification.getRouteId(),
                notification.getPlaceId(),
                notificationTo(notification)
        );
    }

    private String notificationTo(Notification notification) {
        if (notification.getPostId() != null && !notification.getPostId().isBlank()) {
            return "/community/post/" + notification.getPostId();
        }
        if (notification.getRouteId() != null && !notification.getRouteId().isBlank()) {
            return "/ai/courses/" + notification.getRouteId();
        }
        if (notification.getPlaceId() != null && !notification.getPlaceId().isBlank()) {
            return "/places/" + notification.getPlaceId();
        }
        if (notification.getActorUserId() != null) {
            return "/users/" + notification.getActorUserId();
        }
        return "/community";
    }

    private String actorName(User actor) {
        return firstText(actor == null ? null : actor.getNickname(), "sceniK user");
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private String firstText(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
