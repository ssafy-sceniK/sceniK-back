package com.scenik.mapper;

import com.scenik.domain.CommunityComment;
import com.scenik.domain.CommunityPost;
import com.scenik.domain.Notification;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommunityMapper {
    List<CommunityPost> findPosts(@Param("userId") Long userId);
    List<CommunityPost> findPostsByAuthorId(@Param("authorId") Long authorId, @Param("userId") Long userId);
    CommunityPost findPostById(@Param("postId") String postId, @Param("userId") Long userId);
    int insertPost(CommunityPost post);
    int updatePost(CommunityPost post);
    int softDeletePost(@Param("postId") String postId);

    List<String> findTagsByPostId(@Param("postId") String postId);
    int deleteTagsByPostId(@Param("postId") String postId);
    int insertTag(@Param("postId") String postId, @Param("tag") String tag);

    List<CommunityComment> findCommentsByPostId(@Param("postId") String postId, @Param("userId") Long userId);
    CommunityComment findCommentById(@Param("commentId") String commentId, @Param("userId") Long userId);
    int insertComment(CommunityComment comment);
    int updateComment(CommunityComment comment);
    int softDeleteComment(@Param("commentId") String commentId);

    int insertPostLike(@Param("userId") Long userId, @Param("postId") String postId);
    int deletePostLike(@Param("userId") Long userId, @Param("postId") String postId);
    int insertCommentLike(@Param("userId") Long userId, @Param("commentId") String commentId);
    int deleteCommentLike(@Param("userId") Long userId, @Param("commentId") String commentId);

    int existsReport(@Param("userId") Long userId, @Param("postId") String postId);
    int insertReport(@Param("reportId") String reportId, @Param("userId") Long userId, @Param("postId") String postId, @Param("reason") String reason);

    int insertNotification(@Param("notificationId") String notificationId, @Param("userId") Long userId, @Param("title") String title, @Param("message") String message);
    List<Notification> findNotificationsByUserId(@Param("userId") Long userId);
    Notification findNotificationById(@Param("userId") Long userId, @Param("notificationId") String notificationId);
    int countNotificationsByUserId(@Param("userId") Long userId);
    int countUnreadNotificationsByUserId(@Param("userId") Long userId);
    int markNotificationRead(@Param("userId") Long userId, @Param("notificationId") String notificationId);
    int markAllNotificationsRead(@Param("userId") Long userId);
}
