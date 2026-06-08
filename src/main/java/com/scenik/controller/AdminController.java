package com.scenik.controller;

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
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/contents")
    public ContentDto createContent(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ContentUpsertRequest request
    ) {
        return adminService.createContent(authorization, request);
    }

    @PatchMapping("/contents/{contentId}")
    public ContentDto updateContent(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("contentId") String contentId,
            @RequestBody ContentUpsertRequest request
    ) {
        return adminService.updateContent(authorization, contentId, request);
    }

    @DeleteMapping("/contents/{contentId}")
    public ResponseEntity<Void> deleteContent(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("contentId") String contentId
    ) {
        adminService.deleteContent(authorization, contentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/places")
    public PlaceDto createPlace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody PlaceUpsertRequest request
    ) {
        return adminService.createPlace(authorization, request);
    }

    @PatchMapping("/places/{placeId}")
    public PlaceDto updatePlace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId,
            @RequestBody PlaceUpsertRequest request
    ) {
        return adminService.updatePlace(authorization, placeId, request);
    }

    @DeleteMapping("/places/{placeId}")
    public ResponseEntity<Void> deletePlace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("placeId") String placeId
    ) {
        adminService.deletePlace(authorization, placeId);
        return ResponseEntity.noContent().build();
    }
}
