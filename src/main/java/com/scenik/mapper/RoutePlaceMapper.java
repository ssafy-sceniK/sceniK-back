package com.scenik.mapper;

import com.scenik.domain.RoutePlace;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoutePlaceMapper {
    List<RoutePlace> findByRouteId(@Param("routeId") String routeId);
    RoutePlace findById(@Param("routePlaceId") String routePlaceId);
    int existsByRouteAndPlace(@Param("routeId") String routeId, @Param("placeId") String placeId, @Param("excludeRoutePlaceId") String excludeRoutePlaceId);
    int existsByRouteDayOrder(@Param("routeId") String routeId, @Param("dayNumber") Integer dayNumber, @Param("visitOrder") Integer visitOrder, @Param("excludeRoutePlaceId") String excludeRoutePlaceId);
    int insert(RoutePlace place);
    int update(RoutePlace place);
    int updateOrder(@Param("routePlaceId") String routePlaceId, @Param("dayNumber") Integer dayNumber, @Param("visitOrder") Integer visitOrder);
    int delete(@Param("routePlaceId") String routePlaceId);
    int deleteByRouteId(@Param("routeId") String routeId);
}
