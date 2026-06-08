package com.scenik.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsInvalidCredentialsToFriendlyKoreanMessage() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."),
                request()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVALID_CREDENTIALS");
        assertThat(response.getBody().message()).isEqualTo("가입되지 않았거나 비밀번호가 올바르지 않습니다.");
    }

    @Test
    void mapsRoutePlacesRequiredToFriendlyKoreanMessage() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route days are required."),
                request()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVALID_REQUEST");
        assertThat(response.getBody().message()).isEqualTo("코스에 저장할 장소가 없습니다.");
    }

    @Test
    void mapsUnmappedStatusToSafeKoreanMessage() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "Unexpected raw reason."),
                request()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVALID_REQUEST");
        assertThat(response.getBody().message()).isEqualTo("요청을 처리할 수 없습니다. 입력값을 확인해 주세요.");
    }

    private HttpServletRequest request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/test");
        return request;
    }
}
