package com.scenik.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record FieldErrorResponse(String field, String message) {
    }

    public record ErrorResponse(String code, String message, List<FieldErrorResponse> fieldErrors) {
        public ErrorResponse(String code, String message) {
            this(code, message, List.of());
        }
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException exception, HttpServletRequest request) {
        HttpStatus status = resolveStatus(exception.getStatusCode());
        ErrorDescriptor descriptor = descriptorFor(status, exception.getReason());
        if (status.is5xxServerError()) {
            log.error("ResponseStatusException during {} {}", request.getMethod(), request.getRequestURI(), exception);
        } else {
            log.debug("Handled {} during {} {}: {}", status, request.getMethod(), request.getRequestURI(), descriptor.message());
        }
        return ResponseEntity.status(status).body(new ErrorResponse(descriptor.code(), descriptor.message()));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateKey(DuplicateKeyException exception, HttpServletRequest request) {
        log.warn("Duplicate key during {} {}", request.getMethod(), request.getRequestURI(), exception);
        ErrorDescriptor descriptor = duplicateDescriptor(exception);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(descriptor.code(), descriptor.message()));
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleSqlIntegrity(SQLIntegrityConstraintViolationException exception, HttpServletRequest request) {
        log.warn("SQL integrity constraint violation during {} {}", request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLICT", "요청한 데이터가 현재 상태와 충돌합니다."));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(DataAccessException exception, HttpServletRequest request) {
        log.error("Database/schema/query error during {} {}", request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("DB_ERROR", "데이터 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpServletRequest request) {
        log.warn("Validation error during {} {}", request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity.badRequest().body(validationResponse(exception.getBindingResult().getAllErrors()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException exception, HttpServletRequest request) {
        log.warn("Binding validation error during {} {}", request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity.badRequest().body(validationResponse(exception.getBindingResult().getAllErrors()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        log.warn("Constraint validation error during {} {}", request.getMethod(), request.getRequestURI(), exception);
        List<FieldErrorResponse> fieldErrors = exception.getConstraintViolations().stream()
                .map(violation -> new FieldErrorResponse(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", "입력값을 확인해 주세요.", fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
        log.warn("Unreadable request body during {} {}", request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_REQUEST", "요청 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException exception, HttpServletRequest request) {
        log.warn("Missing request parameter during {} {}", request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "VALIDATION_ERROR",
                "입력값을 확인해 주세요.",
                List.of(new FieldErrorResponse(exception.getParameterName(), "필수 요청 값이 누락되었습니다."))
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        log.warn("Request type mismatch during {} {}", request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "VALIDATION_ERROR",
                "입력값을 확인해 주세요.",
                List.of(new FieldErrorResponse(exception.getName(), "입력값 형식이 올바르지 않습니다."))
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        log.warn("Invalid argument during {} {}", request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_REQUEST", "입력값을 확인해 주세요."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternalError(Exception exception, HttpServletRequest request) {
        log.error("Unexpected server error during {} {}", request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }

    private HttpStatus resolveStatus(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        return status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
    }

    private ErrorResponse validationResponse(List<ObjectError> errors) {
        List<FieldErrorResponse> fieldErrors = errors.stream()
                .map(error -> {
                    String field = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
                    return new FieldErrorResponse(field, error.getDefaultMessage());
                })
                .toList();
        return new ErrorResponse("VALIDATION_ERROR", "입력값을 확인해 주세요.", fieldErrors);
    }

    private ErrorDescriptor duplicateDescriptor(Exception exception) {
        String text = String.valueOf(exception.getMessage()).toLowerCase();
        if (text.contains("email") || text.contains("uq_users_email")) {
            return new ErrorDescriptor("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다.");
        }
        if (text.contains("nickname") || text.contains("uq_users_nickname")) {
            return new ErrorDescriptor("DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다.");
        }
        return new ErrorDescriptor("CONFLICT", "요청한 데이터가 현재 상태와 충돌합니다.");
    }

    private ErrorDescriptor descriptorFor(HttpStatus status, String reason) {
        String safeReason = reason == null ? "" : reason.trim();
        return switch (safeReason) {
            case "Email already exists." -> new ErrorDescriptor("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다.");
            case "Nickname already exists." -> new ErrorDescriptor("DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다.");
            case "Invalid email or password." -> new ErrorDescriptor("INVALID_CREDENTIALS", "가입되지 않았거나 비밀번호가 올바르지 않습니다.");
            case "Bearer token is required.", "Refresh token is required." -> new ErrorDescriptor("UNAUTHORIZED", "로그인이 필요합니다.");
            case "Invalid or expired token.", "User was not found." -> new ErrorDescriptor("TOKEN_EXPIRED", "로그인이 만료되었습니다. 다시 로그인해 주세요.");
            case "Password must be at least 8 characters." -> new ErrorDescriptor("VALIDATION_ERROR", "비밀번호는 8자 이상이어야 합니다.");
            case "Email is required." -> new ErrorDescriptor("VALIDATION_ERROR", "이메일을 입력해 주세요.");
            case "Email must be valid." -> new ErrorDescriptor("VALIDATION_ERROR", "올바른 이메일 형식이 아닙니다.");
            case "Nickname is required." -> new ErrorDescriptor("VALIDATION_ERROR", "닉네임을 입력해 주세요.");
            case "contentId is required." -> new ErrorDescriptor("VALIDATION_ERROR", "콘텐츠 ID를 입력해 주세요.");
            case "placeId is required." -> new ErrorDescriptor("VALIDATION_ERROR", "장소 ID를 입력해 주세요.");
            case "Content was not found." -> new ErrorDescriptor("NOT_FOUND", "존재하지 않는 콘텐츠입니다.");
            case "Place was not found." -> new ErrorDescriptor("NOT_FOUND", "존재하지 않는 장소입니다.");
            case "Route place was not found." -> new ErrorDescriptor("NOT_FOUND", "코스에 포함된 장소 정보를 찾을 수 없습니다.");
            case "Route was not found.", "AI route generation was not found." -> new ErrorDescriptor("NOT_FOUND", "존재하지 않는 코스입니다.");
            case "Post was not found." -> new ErrorDescriptor("NOT_FOUND", "존재하지 않는 게시글입니다.");
            case "Comment was not found." -> new ErrorDescriptor("NOT_FOUND", "존재하지 않는 댓글입니다.");
            case "Notification was not found." -> new ErrorDescriptor("NOT_FOUND", "존재하지 않는 알림입니다.");
            case "Review was not found." -> new ErrorDescriptor("NOT_FOUND", "존재하지 않는 리뷰입니다.");
            case "Content already exists." -> new ErrorDescriptor("CONFLICT", "이미 등록된 콘텐츠입니다.");
            case "Place already exists." -> new ErrorDescriptor("CONFLICT", "이미 등록된 장소입니다.");
            case "Place review already exists." -> new ErrorDescriptor("CONFLICT", "이미 이 장소에 리뷰를 작성했습니다.");
            case "Cannot follow self." -> new ErrorDescriptor("VALIDATION_ERROR", "자기 자신은 팔로우할 수 없습니다.");
            case "Follow already exists." -> new ErrorDescriptor("CONFLICT", "이미 팔로우한 사용자입니다.");
            case "Target user was not found." -> new ErrorDescriptor("NOT_FOUND", "존재하지 않는 사용자입니다.");
            case "Post was already reported." -> new ErrorDescriptor("CONFLICT", "이미 신고한 게시글입니다.");
            case "Post owner does not match.", "Comment owner does not match.", "Route owner does not match.", "Review owner does not match." -> new ErrorDescriptor("FORBIDDEN", "권한이 없습니다.");
            case "Review content is required." -> new ErrorDescriptor("VALIDATION_ERROR", "리뷰 내용을 입력해 주세요.");
            case "Review rating must be between 0 and 5." -> new ErrorDescriptor("VALIDATION_ERROR", "평점은 0점 이상 5점 이하로 입력해 주세요.");
            case "Invalid page parameter." -> new ErrorDescriptor("VALIDATION_ERROR", "페이지 번호를 확인해 주세요.");
            case "Invalid size parameter." -> new ErrorDescriptor("VALIDATION_ERROR", "페이지 크기를 확인해 주세요.");
            case "Invalid sort parameter." -> new ErrorDescriptor("VALIDATION_ERROR", "정렬 기준을 확인해 주세요.");
            case "Visit status must be WANT_TO_GO or VISITED." -> new ErrorDescriptor("INVALID_REQUEST", "방문 상태 값이 올바르지 않습니다.");
            case "No verified candidate places were found." -> new ErrorDescriptor("NOT_FOUND", "조건에 맞는 장소를 찾지 못했습니다. 지역이나 콘텐츠 조건을 바꿔보세요.");
            case "durationDays must be positive." -> new ErrorDescriptor("INVALID_REQUEST", "여행 일수는 1일 이상이어야 합니다.");
            case "Route days are required.", "Each day must include places.", "candidatePlaces are required.", "Reorder places are required." -> new ErrorDescriptor("INVALID_REQUEST", "코스에 저장할 장소가 없습니다.");
            case "Route stop placeId is required.", "Candidate placeId is required.", "Route placeId must exist in candidatePlaces." -> new ErrorDescriptor("INVALID_REQUEST", "코스에 포함된 장소 정보를 찾을 수 없습니다.");
            case "Candidate places must be verified." -> new ErrorDescriptor("INVALID_REQUEST", "검증된 장소만 코스에 포함할 수 있습니다.");
            case "Duplicate candidate placeId." -> new ErrorDescriptor("CONFLICT", "중복된 후보 장소가 있습니다.");
            case "Duplicate visitOrder for route day." -> new ErrorDescriptor("CONFLICT", "같은 날짜에 중복된 방문 순서가 있습니다.");
            case "Duplicate placeId in route." -> new ErrorDescriptor("CONFLICT", "이미 코스에 포함된 장소입니다.");
            case "title is required.", "Route title is required." -> new ErrorDescriptor("VALIDATION_ERROR", "제목을 입력해 주세요.");
            case "content is required." -> new ErrorDescriptor("VALIDATION_ERROR", "내용을 입력해 주세요.");
            case "category is required." -> new ErrorDescriptor("VALIDATION_ERROR", "카테고리를 입력해 주세요.");
            case "name is required." -> new ErrorDescriptor("VALIDATION_ERROR", "이름을 입력해 주세요.");
            case "Route region is required." -> new ErrorDescriptor("VALIDATION_ERROR", "지역을 선택해 주세요.");
            case "region is required." -> new ErrorDescriptor("VALIDATION_ERROR", "지역을 입력해 주세요.");
            case "latitude and longitude are required." -> new ErrorDescriptor("VALIDATION_ERROR", "위도와 경도를 입력해 주세요.");
            case "Invalid dayNumber.", "Route day is required.", "dayNumber is out of bounds.", "Stop dayNumber must match parent day." -> new ErrorDescriptor("VALIDATION_ERROR", "코스 날짜가 올바르지 않습니다.");
            case "visitOrder must be positive." -> new ErrorDescriptor("VALIDATION_ERROR", "방문 순서는 1 이상이어야 합니다.");
            case "Route stop is required.", "Route place request is required.", "recommendedTime, reason and travelTip are required." -> new ErrorDescriptor("INVALID_REQUEST", "코스 장소 정보가 올바르지 않습니다.");
            case "Content request is required." -> new ErrorDescriptor("VALIDATION_ERROR", "콘텐츠 정보를 입력해 주세요.");
            case "Place request is required." -> new ErrorDescriptor("VALIDATION_ERROR", "장소 정보를 입력해 주세요.");
            case "Post request is required." -> new ErrorDescriptor("VALIDATION_ERROR", "게시글 내용을 입력해 주세요.");
            case "Comment request is required." -> new ErrorDescriptor("VALIDATION_ERROR", "댓글 내용을 입력해 주세요.");
            case "Report reason is required." -> new ErrorDescriptor("VALIDATION_ERROR", "신고 사유를 선택해 주세요.");
            case "Nested replies are not supported." -> new ErrorDescriptor("INVALID_REQUEST", "대댓글은 아직 지원하지 않습니다.");
            default -> defaultDescriptor(status);
        };
    }

    private ErrorDescriptor defaultDescriptor(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> new ErrorDescriptor("INVALID_REQUEST", "입력값을 확인해 주세요.");
            case UNAUTHORIZED -> new ErrorDescriptor("UNAUTHORIZED", "로그인이 필요합니다.");
            case FORBIDDEN -> new ErrorDescriptor("FORBIDDEN", "권한이 없습니다.");
            case NOT_FOUND -> new ErrorDescriptor("NOT_FOUND", "요청한 정보를 찾을 수 없습니다.");
            case CONFLICT -> new ErrorDescriptor("CONFLICT", "요청한 데이터가 현재 상태와 충돌합니다.");
            default -> status.is5xxServerError()
                    ? new ErrorDescriptor("INTERNAL_ERROR", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
                    : new ErrorDescriptor("INVALID_REQUEST", "요청을 처리할 수 없습니다. 입력값을 확인해 주세요.");
        };
    }

    private record ErrorDescriptor(String code, String message) {
    }
}
