package com.scenik.dto;

import com.scenik.domain.User;

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
}
