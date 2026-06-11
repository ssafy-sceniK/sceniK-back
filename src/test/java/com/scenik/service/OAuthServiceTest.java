package com.scenik.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scenik.domain.OAuthAccount;
import com.scenik.domain.OAuthLoginCode;
import com.scenik.domain.User;
import com.scenik.dto.AuthDtos.AuthResponse;
import com.scenik.dto.UserDto;
import com.scenik.mapper.OAuthAccountMapper;
import com.scenik.mapper.OAuthLoginCodeMapper;
import com.scenik.mapper.UserMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class OAuthServiceTest {
    @Mock UserMapper userMapper;
    @Mock OAuthAccountMapper oauthAccountMapper;
    @Mock OAuthLoginCodeMapper oauthLoginCodeMapper;
    @Mock AuthService authService;

    private OAuthService oauthService;

    @BeforeEach
    void setUp() {
        oauthService = new OAuthService(userMapper, oauthAccountMapper, oauthLoginCodeMapper, authService);
    }

    @Test
    void existingOAuthAccountIssuesOneTimeCode() {
        OAuthAccount account = new OAuthAccount();
        account.setUserId(7L);
        when(oauthAccountMapper.findByProviderUserId("google", "google-user-1")).thenReturn(account);
        when(oauthLoginCodeMapper.insert(any(OAuthLoginCode.class))).thenReturn(1);

        String code = oauthService.completeLogin("google", Map.of("sub", "google-user-1"));

        assertNotNull(code);
        verify(userMapper, never()).findByEmail(any());
        ArgumentCaptor<OAuthLoginCode> captor = ArgumentCaptor.forClass(OAuthLoginCode.class);
        verify(oauthLoginCodeMapper).insert(captor.capture());
        assertEquals(7L, captor.getValue().getUserId());
    }

    @Test
    void verifiedGoogleEmailLinksExistingUser() {
        User user = user(11L, "member@scenik.com", "member");
        when(userMapper.findByEmail("member@scenik.com")).thenReturn(user);
        when(oauthLoginCodeMapper.insert(any(OAuthLoginCode.class))).thenReturn(1);

        oauthService.completeLogin("google", Map.of(
                "sub", "google-user-2",
                "email", "member@scenik.com",
                "email_verified", true,
                "name", "Member"
        ));

        ArgumentCaptor<OAuthAccount> captor = ArgumentCaptor.forClass(OAuthAccount.class);
        verify(oauthAccountMapper).insert(captor.capture());
        assertEquals(11L, captor.getValue().getUserId());
        assertEquals("google-user-2", captor.getValue().getProviderUserId());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void verifiedGoogleEmailCreatesNewUserAndAccount() {
        when(userMapper.findByEmail("new@scenik.com")).thenReturn(null);
        when(userMapper.findByNickname("New Member")).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User inserted = invocation.getArgument(0);
            inserted.setUserId(23L);
            return 1;
        });
        when(oauthLoginCodeMapper.insert(any(OAuthLoginCode.class))).thenReturn(1);

        oauthService.completeLogin("google", Map.of(
                "sub", "google-user-new",
                "email", "new@scenik.com",
                "email_verified", true,
                "name", "New Member",
                "picture", "https://example.com/profile.jpg"
        ));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        assertEquals("new@scenik.com", userCaptor.getValue().getEmail());
        assertEquals("New Member", userCaptor.getValue().getNickname());
        assertEquals("https://example.com/profile.jpg", userCaptor.getValue().getProfileImageUrl());
        assertNotNull(userCaptor.getValue().getPasswordHash());

        ArgumentCaptor<OAuthAccount> accountCaptor = ArgumentCaptor.forClass(OAuthAccount.class);
        verify(oauthAccountMapper).insert(accountCaptor.capture());
        assertEquals(23L, accountCaptor.getValue().getUserId());
    }

    @Test
    void unverifiedEmailIsRejected() {
        assertThrows(ResponseStatusException.class, () -> oauthService.completeLogin("google", Map.of(
                "sub", "google-user-3",
                "email", "member@scenik.com",
                "email_verified", false
        )));

        verify(oauthAccountMapper, never()).insert(any());
    }

    @Test
    void activeCodeIsConsumedAndExchangedForSession() {
        OAuthLoginCode loginCode = new OAuthLoginCode();
        loginCode.setOauthLoginCodeId(3L);
        loginCode.setUserId(19L);
        AuthResponse response = new AuthResponse(
                "access",
                "refresh",
                UserDto.from(user(19L, "oauth@scenik.com", "oauth-user"))
        );
        when(oauthLoginCodeMapper.findActiveByHashForUpdate(any())).thenReturn(loginCode);
        when(oauthLoginCodeMapper.consume(3L)).thenReturn(1);
        when(authService.issueSession(19L)).thenReturn(response);

        AuthResponse exchanged = oauthService.exchangeCode("one-time-code");

        assertEquals("access", exchanged.accessToken());
        verify(oauthLoginCodeMapper).consume(3L);
        verify(authService).issueSession(19L);
    }

    private User user(Long userId, String email, String nickname) {
        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setNickname(nickname);
        return user;
    }
}
