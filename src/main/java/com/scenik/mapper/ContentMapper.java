package com.scenik.mapper;

import com.scenik.domain.Content;
import com.scenik.domain.Place;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ContentMapper {
    List<Content> findContents(@Param("category") String category, @Param("keyword") String keyword, @Param("popular") Boolean popular, @Param("userId") Long userId);
    List<String> findCategories();
    Content findById(@Param("contentId") String contentId, @Param("userId") Long userId);
    Content findByIdForWrite(@Param("contentId") String contentId);
    List<Place> findPlacesByContentId(@Param("contentId") String contentId);
    List<Content> findByPlaceId(@Param("placeId") String placeId, @Param("userId") Long userId);
    int insert(Content content);
    int update(Content content);
    int softDelete(@Param("contentId") String contentId);
}
