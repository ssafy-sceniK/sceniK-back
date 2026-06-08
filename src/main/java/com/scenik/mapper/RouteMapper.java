package com.scenik.mapper;

import com.scenik.domain.TravelRoute;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RouteMapper {
    List<TravelRoute> findByUserId(@Param("userId") Long userId);
    TravelRoute findById(@Param("routeId") String routeId);
    int insert(TravelRoute route);
    int update(TravelRoute route);
    int delete(@Param("routeId") String routeId);
}
