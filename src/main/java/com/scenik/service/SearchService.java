package com.scenik.service;

import com.scenik.dto.ContentDto;
import com.scenik.dto.PlaceDto;
import com.scenik.dto.RelatedContentDto;
import com.scenik.dto.SearchResultDto;
import com.scenik.mapper.ContentMapper;
import com.scenik.mapper.PlaceMapper;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
    private static final int RESULT_LIMIT = 20;

    private final ContentMapper contentMapper;
    private final PlaceMapper placeMapper;
    private final SystemLocalizationService localizationService;

    public SearchService(ContentMapper contentMapper, PlaceMapper placeMapper, SystemLocalizationService localizationService) {
        this.contentMapper = contentMapper;
        this.placeMapper = placeMapper;
        this.localizationService = localizationService;
    }

    public SearchResultDto search(String keyword, String acceptLanguage) {
        String query = normalizeSearchText(keyword);
        String locale = localizationService.normalizeLocale(acceptLanguage);

        List<ContentDto> contents = contentMapper.findContents(null, null, null, null).stream()
                .map(ContentDto::from)
                .map(content -> localizationService.localize(content, locale))
                .filter(content -> matchesContent(content, query))
                .limit(RESULT_LIMIT)
                .toList();

        List<PlaceDto> places = placeMapper.findPlaces(null, null, null, null, null).stream()
                .map(PlaceDto::from)
                .map(place -> localizationService.localize(place, locale))
                .filter(place -> matchesPlace(place, query))
                .limit(RESULT_LIMIT)
                .toList();

        return new SearchResultDto(contents, places);
    }

    private boolean matchesContent(ContentDto content, String query) {
        if (query.isBlank()) return true;
        return containsAny(query,
                content.contentId(),
                content.title(),
                content.originalTitle(),
                content.category(),
                content.originalCategory(),
                content.subcategory(),
                content.originalSubcategory(),
                content.description(),
                content.originalDescription(),
                content.castNames(),
                content.originalCastNames(),
                content.country(),
                content.ottPlatform(),
                joined(content.tags()),
                joined(content.originalTags())
        );
    }

    private boolean matchesPlace(PlaceDto place, String query) {
        if (query.isBlank()) return true;
        return containsAny(query,
                place.placeId(),
                place.name(),
                place.originalName(),
                place.placeName(),
                place.originalPlaceName(),
                place.address(),
                place.region(),
                place.originalRegion(),
                place.district(),
                place.originalDistrict(),
                place.category(),
                place.originalCategory(),
                place.description(),
                place.originalDescription(),
                relatedContentText(place.relatedContents())
        );
    }

    private boolean containsAny(String query, String... values) {
        return Stream.of(values)
                .filter(Objects::nonNull)
                .map(this::normalizeSearchText)
                .anyMatch(value -> value.contains(query));
    }

    private String joined(List<String> values) {
        return values == null ? "" : String.join(" ", values);
    }

    private String relatedContentText(List<RelatedContentDto> relatedContents) {
        if (relatedContents == null || relatedContents.isEmpty()) return "";
        return String.join(" ", relatedContents.stream()
                .flatMap(content -> Stream.of(content.contentId(), content.title(), content.originalTitle()))
                .filter(Objects::nonNull)
                .toList());
    }

    private String normalizeSearchText(String value) {
        return Normalizer.normalize(String.valueOf(value == null ? "" : value), Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }
}
