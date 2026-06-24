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
    int updateProfile(@Param("userId") Long userId, @Param("nickname") String nickname, @Param("bio") String bio, @Param("preferences") String preferences, @Param("language") String language);
    int updateProfileImage(@Param("userId") Long userId, @Param("profileImageUrl") String profileImageUrl);
    int clearProfileImage(@Param("userId") Long userId);
    int updatePassword(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);
    int deleteFollows(@Param("userId") Long userId);
    int deleteRoutes(@Param("userId") Long userId);
    int deleteContentLikes(@Param("userId") Long userId);
    int deletePlaceLikes(@Param("userId") Long userId);
    int deleteVisitStatuses(@Param("userId") Long userId);
    int deletePlaceReviews(@Param("userId") Long userId);
    int deletePostLikes(@Param("userId") Long userId);
    int deletePostScraps(@Param("userId") Long userId);
    int deleteCommentLikes(@Param("userId") Long userId);
    int deleteReports(@Param("userId") Long userId);
    int deleteNotifications(@Param("userId") Long userId);
    int clearTranslationCacheUser(@Param("userId") Long userId);
    int deleteOAuthAccounts(@Param("userId") Long userId);
    int deleteOAuthLoginCodes(@Param("userId") Long userId);
    int deleteRoles(@Param("userId") Long userId);
    int anonymizeAndDeactivate(
            @Param("userId") Long userId,
            @Param("email") String email,
            @Param("nickname") String nickname,
            @Param("passwordHash") String passwordHash
    );
}
