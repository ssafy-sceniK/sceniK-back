package com.scenik.dto;

public record ContentCategoryDto(String categoryId, String name) {
    public static ContentCategoryDto from(String category) {
        return new ContentCategoryDto(category, category);
    }
}
