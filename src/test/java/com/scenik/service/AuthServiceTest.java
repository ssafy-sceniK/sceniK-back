package com.scenik.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scenik.domain.AuthToken;
import com.scenik.domain.User;
import com.scenik.mapper.AuthTokenMapper;
import com.scenik.mapper.PasswordResetTokenMapper;
import com.scenik.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserMapper userMapper;
    @Mock AuthTokenMapper authTokenMapper;
    @Mock PasswordService passwordService;
    @Mock ProfileImageStorageService profileImageStorageService;
    @Mock PasswordResetTokenMapper passwordResetTokenMapper;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userMapper, authTokenMapper, passwordService, profileImageStorageService, passwordResetTokenMapper);
    }

    @Test
    void deleteMeInvalidatesActivePasswordResetTokens() {
        User user = user(7L);
        when(authTokenMapper.findActiveByHash(org.mockito.ArgumentMatchers.anyString())).thenReturn(token(7L));
        when(userMapper.findById(7L)).thenReturn(user);

        service.deleteMe("Bearer raw-token");

        verify(passwordResetTokenMapper).invalidateActiveByUserId(7L);
    }

    @Test
    void deleteMeAnonymizesUserAndClearsPersonalData() {
        User user = user(7L);
        when(authTokenMapper.findActiveByHash(org.mockito.ArgumentMatchers.anyString())).thenReturn(token(7L));
        when(userMapper.findById(7L)).thenReturn(user);

        service.deleteMe("Bearer raw-token");

        verify(profileImageStorageService).deleteManagedImage("/api/uploads/profile/user.png");
        verify(authTokenMapper).revokeByUserId(7L);
        verify(userMapper).deleteFollows(7L);
        verify(userMapper).deleteRoutes(7L);
        verify(userMapper).deleteContentLikes(7L);
        verify(userMapper).deletePlaceLikes(7L);
        verify(userMapper).deleteVisitStatuses(7L);
        verify(userMapper).deletePlaceReviews(7L);
        verify(userMapper).deletePostLikes(7L);
        verify(userMapper).deletePostScraps(7L);
        verify(userMapper).deleteCommentLikes(7L);
        verify(userMapper).deleteReports(7L);
        verify(userMapper).deleteNotifications(7L);
        verify(userMapper).clearTranslationCacheUser(7L);
        verify(userMapper).deleteOAuthLoginCodes(7L);
        verify(userMapper).deleteOAuthAccounts(7L);
        verify(userMapper).deleteRoles(7L);
        verify(userMapper).anonymizeAndDeactivate(7L, "deleted-7@deleted.scenik.local", "deleted-user-7", "deleted$7");
    }

    private User user(Long userId) {
        User user = new User();
        user.setUserId(userId);
        user.setEmail("user@example.com");
        user.setNickname("User");
        user.setPasswordHash("hash");
        user.setStatus("ACTIVE");
        user.setProfileImageUrl("/api/uploads/profile/user.png");
        return user;
    }

    private AuthToken token(Long userId) {
        AuthToken token = new AuthToken();
        token.setUserId(userId);
        return token;
    }
}