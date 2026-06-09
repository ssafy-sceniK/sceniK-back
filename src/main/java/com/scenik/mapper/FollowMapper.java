package com.scenik.mapper;

import com.scenik.domain.User;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FollowMapper {
    int insertFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
    int deleteFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
    int existsFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
    int countFollowers(@Param("userId") Long userId);
    int countFollowing(@Param("userId") Long userId);
    List<User> findFollowers(@Param("userId") Long userId);
    List<User> findFollowing(@Param("userId") Long userId);
}
