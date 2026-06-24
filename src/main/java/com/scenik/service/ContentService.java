package com.scenik.service;

import com.scenik.dto.ContentCategoryDto;
import com.scenik.dto.ContentDto;
import com.scenik.dto.PlaceDto;
import com.scenik.mapper.ContentMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ContentService {
    private final ContentMapper contentMapper;
    private final SystemLocalizationService localizationService;

    public ContentService(ContentMapper contentMapper, SystemLocalizationService localizationService) {
        this.contentMapper = contentMapper;
        this.localizationService = localizationService;
    }

    public List<ContentDto> getContents(String category, String keyword, Boolean popular, Long userId, String acceptLanguage) {
        return contentMapper.findContents(category, keyword, popular, userId).stream()
                .map(ContentDto::from)
                .map(content -> localizationService.localize(content, acceptLanguage))
                .toList();
    }

    public List<ContentCategoryDto> getCategories(String acceptLanguage) {
        return contentMapper.findCategories().stream()
                .map(ContentCategoryDto::from)
                .map(category -> localizationService.localize(category, acceptLanguage))
                .toList();
    }

    public ContentDto getContent(String contentId, Long userId, String acceptLanguage) {
        var content = contentMapper.findById(contentId, userId);
        if (content == null) return null;
        return localizationService.localize(ContentDto.from(content), acceptLanguage);
    }

    public List<PlaceDto> getRelatedPlaces(String contentId, String acceptLanguage) {
        return contentMapper.findPlacesByContentId(contentId).stream()
                .map(PlaceDto::from)
                .map(place -> localizationService.localize(place, acceptLanguage))
                .toList();
    }
}
