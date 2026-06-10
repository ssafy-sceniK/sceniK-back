package com.scenik.config;

import com.scenik.service.OAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuthLoginHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {
    private static final Logger log = LoggerFactory.getLogger(OAuthLoginHandler.class);

    private final OAuthService oauthService;
    private final String frontendRedirectUri;

    public OAuthLoginHandler(
            OAuthService oauthService,
            @Value("${scenik.oauth.frontend-redirect-uri}") String frontendRedirectUri
    ) {
        this.oauthService = oauthService;
        this.frontendRedirectUri = frontendRedirectUri;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User principal = oauthToken.getPrincipal();
            String code = oauthService.completeLogin(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    principal.getAttributes()
            );
            response.sendRedirect(redirectUri("code", code));
        } catch (Exception ex) {
            log.error("OAuth account processing failed", ex);
            response.sendRedirect(redirectUri("error", "oauth_account_processing_failed"));
        }
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        log.warn("OAuth authentication failed: {}", exception.getClass().getSimpleName());
        response.sendRedirect(redirectUri("error", "oauth_authentication_failed"));
    }

    private String redirectUri(String key, String value) {
        return UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam(key, value)
                .build()
                .encode()
                .toUriString();
    }
}
