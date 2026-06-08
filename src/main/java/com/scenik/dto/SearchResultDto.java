package com.scenik.dto;

import java.util.List;

public record SearchResultDto(
        List<ContentDto> contents,
        List<PlaceDto> places
) {
}
