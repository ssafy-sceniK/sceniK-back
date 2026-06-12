package com.scenik.controller;

import com.scenik.dto.ApiResponse;
import com.scenik.dto.AdminDtos.ContentUpsertRequest;
import com.scenik.dto.AdminDtos.PlaceUpsertRequest;
import com.scenik.dto.ContentDto;
import com.scenik.dto.PlaceDto;
import com.scenik.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final String CREATED_MESSAGE = "생성되었습니다.";
    private static final String UPDATED_MESSAGE = "수정되었습니다.";
    private static final String DELETED_MESSAGE = "삭제되었습니다.";

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/contents")
    public ApiResponse<ContentDto> createContent(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ContentUpsertRequest request
    ) {
        return ApiResponse.ok(CREATED_MESSAGE, adminService.createContent(authorization, request));
    }

    @PatchMapping("/contents/{contentId}")
    public ApiResponse<ContentDto> updateContent(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("contentId") String contentId,
            @RequestBody ContentUpsertRequest request
    ) {
        return ApiResponse.ok(UPDATED_MESSAGE, adminService.updateContent(authorization, contentId, request));
    }

    @DeleteMapping("/contents/{contentId}")
    public ResponseEntity<ApiResponse<Void>> deleteContent(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("contentId") String contentId
    ) {
        adminService.deleteContent(authorization, contentId);
        return ResponseEntity.ok(ApiResponse.okMessage(DELETED_MESSAGE));
    }

    @PostMapping("/places")
    public ApiResponse<PlaceDto> createPlace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody PlaceUpsertRequest request
    ) {
        return ApiResponse.ok(CREATED_MESSAGE, adminService.createPlace(authorization, request));
    }

    @PatchMapping("/places/{placeId}")
    public ApiResponse<PlaceDto> updatePlace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId,
            @RequestBody PlaceUpsertRequest request
    ) {
        return ApiResponse.ok(UPDATED_MESSAGE, adminService.updatePlace(authorization, placeId, request));
    }

    @DeleteMapping("/places/{placeId}")
    public ResponseEntity<ApiResponse<Void>> deletePlace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId
    ) {
        adminService.deletePlace(authorization, placeId);
        return ResponseEntity.ok(ApiResponse.okMessage(DELETED_MESSAGE));
    }
}
