package com.scenik.mapper;

import com.scenik.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findById(@Param("userId") Long userId);
    User findByEmail(@Param("email") String email);
    User findByNickname(@Param("nickname") String nickname);
    int insert(User user);
    int updateProfile(@Param("userId") Long userId, @Param("nickname") String nickname, @Param("bio") String bio, @Param("preferences") String preferences);
    int updateProfileImage(@Param("userId") Long userId, @Param("profileImageUrl") String profileImageUrl);
}
