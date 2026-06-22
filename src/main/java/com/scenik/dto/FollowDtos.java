package com.scenik.dto;

import com.scenik.domain.User;
import com.scenik.dto.CommunityDtos.PostDto;
import com.scenik.dto.RouteDtos.AiRouteDto;
import java.util.List;

public final class FollowDtos {
    private FollowDtos() {
    }

    public record FollowUserDto(
            Long userId,
            String nickname,
            Integer followerCount,
            Integer followingCount,
            Boolean followedByMe
    ) {
        public static FollowUserDto from(User user, Integer followerCount, Integer followingCount, Boolean followedByMe) {
            return new FollowUserDto(
                    user.getUserId(),
                    user.getNickname(),
                    followerCount == null ? 0 : followerCount,
                    followingCount == null ? 0 : followingCount,
                    Boolean.TRUE.equals(followedByMe)
            );
        }
    }

    public record FollowStatusDto(
            Long userId,
            Boolean followedByMe,
            Integer followerCount,
            Integer followingCount
    ) {
    }

    public record PublicUserProfileDto(
            UserDto user,
            Integer followerCount,
            Integer followingCount,
            Boolean followedByMe,
            List<AiRouteDto> savedCourses,
            List<PostDto> posts
    ) {
    }
}
