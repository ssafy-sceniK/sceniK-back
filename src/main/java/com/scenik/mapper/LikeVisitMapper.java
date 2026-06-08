package com.scenik.mapper;

import com.scenik.domain.VisitStatus;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeVisitMapper {
    int insertContentLike(@Param("userId") Long userId, @Param("contentId") String contentId);
    int deleteContentLike(@Param("userId") Long userId, @Param("contentId") String contentId);
    int insertPlaceLike(@Param("userId") Long userId, @Param("placeId") String placeId);
    int deletePlaceLike(@Param("userId") Long userId, @Param("placeId") String placeId);
    List<String> findLikedContentIds(@Param("userId") Long userId);
    List<String> findLikedPlaceIds(@Param("userId") Long userId);
    int upsertVisitStatus(@Param("userId") Long userId, @Param("placeId") String placeId, @Param("status") String status);
    int deleteVisitStatus(@Param("userId") Long userId, @Param("placeId") String placeId);
    List<VisitStatus> findVisitStatuses(@Param("userId") Long userId);
}
