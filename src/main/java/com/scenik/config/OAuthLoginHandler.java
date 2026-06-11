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
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
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
            response.sendRedirect(redirectUri(oauthToken.getAuthorizedClientRegistrationId(), "code", code));
        } catch (Exception ex) {
            log.error("OAuth account processing failed", ex);
            response.sendRedirect(redirectUri(providerFrom(request), "error", processingError(ex)));
        }
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        log.warn("OAuth authentication failed: {}", exception.getClass().getSimpleName());
        String errorCode = exception instanceof OAuth2AuthenticationException oauthException
                ? oauthException.getError().getErrorCode()
                : "oauth_authentication_failed";
        response.sendRedirect(redirectUri(providerFrom(request), "error", errorCode));
    }

    private String redirectUri(String provider, String key, String value) {
        return UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("provider", provider)
                .queryParam(key, value)
                .build()
                .encode()
                .toUriString();
    }

    private String providerFrom(HttpServletRequest request) {
        String uri = request.getRequestURI();
        int separator = uri.lastIndexOf('/');
        return separator >= 0 && separator < uri.length() - 1 ? uri.substring(separator + 1) : "oauth";
    }

    private String processingError(Exception exception) {
        String message = exception.getMessage();
        if (message != null && message.contains("OAuth email is required")) return "oauth_email_required";
        if (message != null && message.contains("OAuth email is not verified")) return "oauth_email_not_verified";
        return "oauth_account_processing_failed";
    }
}
