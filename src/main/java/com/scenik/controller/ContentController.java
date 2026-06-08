package com.scenik.controller;

import com.scenik.dto.ContentCategoryDto;
import com.scenik.dto.ContentDto;
import com.scenik.dto.PlaceDto;
import com.scenik.service.AuthService;
import com.scenik.service.ContentService;
import com.scenik.util.ListQuerySupport;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/contents")
public class ContentController {
    private final AuthService authService;
    private final ContentService contentService;

    public ContentController(AuthService authService, ContentService contentService) {
        this.authService = authService;
        this.contentService = contentService;
    }

    @GetMapping
    public ResponseEntity<List<ContentDto>> list(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "popular", required = false) Boolean popular,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        ListQuerySupport.validateSort(sort, Set.of("popular", "latest"));
        String normalizedSort = ListQuerySupport.normalizeSort(sort);
        Boolean popularOnly = popular != null ? popular : ("popular".equalsIgnoreCase(sort) ? Boolean.TRUE : null);
        List<ContentDto> contents = contentService.getContents(category, keyword, popularOnly, authService.optionalUserId(authorization));
        if ("popular".equals(normalizedSort)) {
            contents = contents.stream()
                    .sorted(Comparator.comparingInt((ContentDto content) -> content.likeCount() == null ? 0 : content.likeCount()).reversed())
                    .toList();
        }
        List<ContentDto> paged = ListQuerySupport.paginate(contents, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(contents.size()))
                .body(paged);
    }

    @GetMapping("/categories")
    public List<ContentCategoryDto> categories() {
        return contentService.getCategories();
    }

    @GetMapping("/popular")
    public List<ContentDto> popular(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return contentService.getContents(null, null, Boolean.TRUE, authService.optionalUserId(authorization));
    }

    @GetMapping("/{contentId}")
    public ContentDto detail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("contentId") String contentId
    ) {
        var content = contentService.getContent(contentId, authService.optionalUserId(authorization));
        if (content == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Content was not found.");
        }
        return content;
    }

    @GetMapping("/{contentId}/places")
    public List<PlaceDto> relatedPlaces(@PathVariable("contentId") String contentId) {
        return contentService.getRelatedPlaces(contentId);
    }
}
