package com.scenik.dto;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data
) {
    private static final String DEFAULT_SUCCESS_CODE = "SUCCESS";
    private static final String DEFAULT_SUCCESS_MESSAGE = "요청이 성공했습니다.";

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, DEFAULT_SUCCESS_CODE, DEFAULT_SUCCESS_MESSAGE, data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, DEFAULT_SUCCESS_CODE, message, data);
    }

    public static ApiResponse<Void> okMessage(String message) {
        return new ApiResponse<>(true, DEFAULT_SUCCESS_CODE, message, null);
    }
}
