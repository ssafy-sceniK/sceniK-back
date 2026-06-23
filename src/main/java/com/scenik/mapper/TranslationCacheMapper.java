package com.scenik.mapper;

import com.scenik.domain.TranslationCache;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TranslationCacheMapper {
    TranslationCache findLatest(
            @Param("resourceType") String resourceType,
            @Param("resourceId") String resourceId,
            @Param("fieldName") String fieldName,
            @Param("sourceLanguage") String sourceLanguage,
            @Param("targetLanguage") String targetLanguage,
            @Param("sourceHash") String sourceHash
    );

    int insert(TranslationCache cache);
}