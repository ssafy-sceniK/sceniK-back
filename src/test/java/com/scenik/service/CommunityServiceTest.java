package com.scenik.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scenik.domain.CommunityComment;
import com.scenik.domain.CommunityPost;
import com.scenik.domain.User;
import com.scenik.dto.CommunityDtos.CommentRequest;
import com.scenik.mapper.CommunityMapper;
import com.scenik.mapper.RouteMapper;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {
    @Mock AuthService authService;
    @Mock CommunityMapper communityMapper;
    @Mock RouteMapper routeMapper;
    @Mock LanguageDetectionService languageDetectionService;

    private CommunityService service;

    @BeforeEach
    void setUp() {
        service = new CommunityService(authService, communityMapper, routeMapper, languageDetectionService);
    }

    @Test
    void likePostDoesNotCreateDuplicateNotificationWhenLikeAlreadyExists() {
        CommunityPost post = post("post-1", 1L);
        when(communityMapper.findPostById("post-1", 2L)).thenReturn(post);
        when(communityMapper.insertPostLike(2L, "post-1")).thenReturn(0);
        stubPostDtoDependencies("post-1", 2L);

        service.likePost(user(2L, "Liker"), "post-1");

        verify(communityMapper, never()).insertNotification(anyString(), anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), any(), anyLong(), any(), any());
    }

    @Test
    void unlikePostDeletesMatchingLikeNotification() {
        CommunityPost post = post("post-1", 1L);
        when(communityMapper.findPostById("post-1", 2L)).thenReturn(post);
        when(communityMapper.deletePostLike(2L, "post-1")).thenReturn(1);
        stubPostDtoDependencies("post-1", 2L);

        service.unlikePost(user(2L, "Liker"), "post-1");

        verify(communityMapper).deletePostLikeNotification(2L, 1L, "post-1");
    }

    @Test
    void deleteCommentDeletesNotificationsForCommentThread() {
        CommunityComment comment = comment("comment-1", "post-1", 2L, null);
        when(communityMapper.findCommentById("comment-1", 2L)).thenReturn(comment);

        service.deleteComment(user(2L, "Commenter"), "post-1", "comment-1");

        verify(communityMapper).deleteNotificationsByCommentThread("post-1", "comment-1");
    }

    @Test
    void replyCreationNotifiesParentCommentAuthor() {
        CommunityPost post = post("post-1", 1L);
        CommunityComment parent = comment("comment-parent", "post-1", 2L, null);
        AtomicReference<CommunityComment> inserted = new AtomicReference<>();
        when(communityMapper.findPostById("post-1", 3L)).thenReturn(post);
        when(communityMapper.findCommentById(anyString(), eq(3L))).thenAnswer(invocation -> {
            String commentId = invocation.getArgument(0);
            if ("comment-parent".equals(commentId)) return parent;
            return inserted.get();
        });
        when(languageDetectionService.detect(anyString(), any())).thenReturn("ko");
        when(communityMapper.insertComment(any())).thenAnswer(invocation -> {
            inserted.set(invocation.getArgument(0));
            return 1;
        });

        service.createComment(user(3L, "Replier"), "post-1", new CommentRequest("reply", null, "comment-parent"));

        verify(communityMapper).insertNotification(anyString(), eq(2L), anyString(), anyString(), eq("COMMENT_REPLY"), eq("post-1"), eq("post-1"), anyString(), eq(3L), any(), any());
    }

    @Test
    void replyCreationUsesReplyNotificationWhenParentAuthorOwnsPost() {
        CommunityPost post = post("post-1", 1L);
        CommunityComment parent = comment("comment-parent", "post-1", 1L, null);
        AtomicReference<CommunityComment> inserted = new AtomicReference<>();
        when(communityMapper.findPostById("post-1", 2L)).thenReturn(post);
        when(communityMapper.findCommentById(anyString(), eq(2L))).thenAnswer(invocation -> {
            String commentId = invocation.getArgument(0);
            if ("comment-parent".equals(commentId)) return parent;
            return inserted.get();
        });
        when(languageDetectionService.detect(anyString(), any())).thenReturn("ko");
        when(communityMapper.insertComment(any())).thenAnswer(invocation -> {
            inserted.set(invocation.getArgument(0));
            return 1;
        });

        service.createComment(user(2L, "Replier"), "post-1", new CommentRequest("reply", null, "comment-parent"));

        verify(communityMapper).insertNotification(anyString(), eq(1L), anyString(), anyString(), eq("COMMENT_REPLY"), eq("post-1"), eq("post-1"), anyString(), eq(2L), any(), any());
        verify(communityMapper, never()).insertNotification(anyString(), eq(1L), anyString(), anyString(), eq("POST_COMMENT"), eq("post-1"), eq("post-1"), anyString(), eq(2L), any(), any());
    }

    private void stubPostDtoDependencies(String postId, Long viewerId) {
        when(communityMapper.findTagsByPostId(postId)).thenReturn(List.of());
        when(communityMapper.findImagesByPostId(postId)).thenReturn(List.of());
        when(communityMapper.findCommentsByPostId(postId, viewerId)).thenReturn(List.of());
    }

    private CommunityPost post(String postId, Long ownerId) {
        CommunityPost post = new CommunityPost();
        post.setPostId(postId);
        post.setUserId(ownerId);
        post.setUserNickname("Owner");
        post.setTitle("Title");
        post.setContent("Content");
        post.setRegion("Korea");
        post.setLikeCount(1);
        post.setScrapCount(0);
        post.setCommentCount(0);
        return post;
    }

    private CommunityComment comment(String commentId, String postId, Long userId, String parentCommentId) {
        CommunityComment comment = new CommunityComment();
        comment.setCommentId(commentId);
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setUserNickname("Commenter");
        comment.setParentCommentId(parentCommentId);
        comment.setContent("Comment");
        comment.setLikeCount(0);
        return comment;
    }

    private User user(Long userId, String nickname) {
        User user = new User();
        user.setUserId(userId);
        user.setNickname(nickname);
        user.setLanguage("ko");
        return user;
    }
}
