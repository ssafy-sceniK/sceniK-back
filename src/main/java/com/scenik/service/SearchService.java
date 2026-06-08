package com.scenik.service;

import com.scenik.dto.ContentDto;
import com.scenik.dto.PlaceDto;
import com.scenik.dto.SearchResultDto;
import com.scenik.mapper.SearchMapper;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
    private final SearchMapper searchMapper;

    public SearchService(SearchMapper searchMapper) {
        this.searchMapper = searchMapper;
    }

    public SearchResultDto search(String keyword) {
        var contents = searchMapper.searchContents(keyword).stream().map(ContentDto::from).toList();
        var places = searchMapper.searchPlaces(keyword).stream().map(PlaceDto::from).toList();
        return new SearchResultDto(contents, places);
    }
}
