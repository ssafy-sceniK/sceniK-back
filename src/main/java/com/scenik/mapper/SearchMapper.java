package com.scenik.mapper;

import com.scenik.domain.Content;
import com.scenik.domain.Place;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SearchMapper {
    List<Content> searchContents(@Param("keyword") String keyword);
    List<Place> searchPlaces(@Param("keyword") String keyword);
}
