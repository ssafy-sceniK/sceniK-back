package com.scenik.service;

import com.scenik.dto.ContentDto;
import com.scenik.dto.ContentCategoryDto;
import com.scenik.dto.PlaceDto;
import com.scenik.mapper.ContentMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ContentService {
    private final ContentMapper contentMapper;

    public ContentService(ContentMapper contentMapper) {
        this.contentMapper = contentMapper;
    }

    public List<ContentDto> getContents(String category, String keyword, Boolean popular, Long userId) {
        return contentMapper.findContents(category, keyword, popular, userId).stream().map(ContentDto::from).toList();
    }

    public List<ContentCategoryDto> getCategories() {
        return contentMapper.findCategories().stream().map(ContentCategoryDto::from).toList();
    }

    public ContentDto getContent(String contentId, Long userId) {
        var content = contentMapper.findById(contentId, userId);
        if (content == null) return null;
        return ContentDto.from(content);
    }

    public List<PlaceDto> getRelatedPlaces(String contentId) {
        return contentMapper.findPlacesByContentId(contentId).stream().map(PlaceDto::from).toList();
    }
}
