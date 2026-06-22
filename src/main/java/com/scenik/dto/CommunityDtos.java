package com.scenik.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public final class CommunityDtos {
    private CommunityDtos() {
    }

    public record PostRequest(
            @NotBlank(message = "제목을 입력해 주세요.")
            @Size(max = 120, message = "제목은 120자 이하로 입력해 주세요.")
            String title,
            String content,
            String body,
            String region,
            String routeId,
            String imageUrl,
            List<String> tags
    ) {
        @AssertTrue(message = "내용을 입력해 주세요.")
        public boolean hasContent() {
            return (content != null && !content.isBlank()) || (body != null && !body.isBlank());
        }
    }

    public record PostDto(
            String postId,
            String id,
            Long userId,
            String user,
            String nickname,
            String authorNickname,
            String title,
            String content,
            String body,
            String region,
            String routeId,
            String imageUrl,
            List<String> tags,
            Integer likeCount,
            Integer likes,
            Boolean isLiked,
            Integer scrapCount,
            Boolean isScrapped,
            List<Long> scrappedBy,
            Integer commentCount,
            List<CommentDto> comments,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record ImageUploadDto(
            String imageUrl,
            String fileName
    ) {
    }

    public record CommentRequest(
            String content,
            String text,
            String parentCommentId
    ) {
        @AssertTrue(message = "댓글 내용을 입력해 주세요.")
        public boolean hasContent() {
            return (content != null && !content.isBlank()) || (text != null && !text.isBlank());
        }
    }

    public record CommentDto(
            String commentId,
            String id,
            String postId,
            Long userId,
            String user,
            String nickname,
            String authorNickname,
            String content,
            String text,
            Integer likeCount,
            Integer likes,
            Boolean likedByMe,
            Boolean isLiked,
            String parentCommentId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record ReportRequest(@NotBlank(message = "신고 사유를 선택해 주세요.") String reason) {
    }

    public record ReportDto(
            String reportId,
            Long reporterId,
            String targetType,
            String targetId,
            String postId,
            String reason,
            String status,
            LocalDateTime createdAt
    ) {
    }

    public record NotificationDto(
            String notificationId,
            String id,
            String title,
            String message,
            String subtitle,
            Boolean isRead,
            Boolean read,
            LocalDateTime createdAt,
            String time,
            String targetType,
            String targetId,
            String postId,
            String commentId,
            Long actorUserId,
            String routeId,
            String placeId,
            String to
    ) {
    }
}
