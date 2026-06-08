package com.scenik.mapper;

import com.scenik.domain.CandidatePlace;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiRouteMapper {
    List<CandidatePlace> findCandidatePlaces(@Param("contentIds") List<String> contentIds, @Param("region") String region, @Param("limit") Integer limit);
}
