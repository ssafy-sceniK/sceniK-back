package com.scenik.service;

import com.scenik.domain.User;
import com.scenik.dto.CommunityDtos.PostDto;
import com.scenik.dto.FollowDtos.FollowStatusDto;
import com.scenik.dto.FollowDtos.FollowUserDto;
import com.scenik.dto.FollowDtos.PublicUserProfileDto;
import com.scenik.dto.RouteDtos.AiRouteDto;
import com.scenik.dto.UserDto;
import com.scenik.mapper.FollowMapper;
import com.scenik.mapper.UserMapper;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FollowService {
    private final FollowMapper followMapper;
    private final UserMapper userMapper;

    public FollowService(FollowMapper followMapper, UserMapper userMapper) {
        this.followMapper = followMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public FollowStatusDto follow(User actor, Long targetUserId) {
        requireTarget(actor, targetUserId);
        if (followMapper.existsFollow(actor.getUserId(), targetUserId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Follow already exists.");
        }
        followMapper.insertFollow(actor.getUserId(), targetUserId);
        return status(actor, targetUserId);
    }

    @Transactional
    public FollowStatusDto unfollow(User actor, Long targetUserId) {
        requireTarget(actor, targetUserId);
        followMapper.deleteFollow(actor.getUserId(), targetUserId);
        return status(actor, targetUserId);
    }

    public List<FollowUserDto> followers(User actor, Long userId) {
        requireUser(userId);
        Long actorUserId = actor == null ? null : actor.getUserId();
        return followMapper.findFollowers(userId, actorUserId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<FollowUserDto> following(User actor, Long userId) {
        requireUser(userId);
        Long actorUserId = actor == null ? null : actor.getUserId();
        return followMapper.findFollowing(userId, actorUserId).stream()
                .map(this::toDto)
                .toList();
    }

    public FollowStatusDto status(User actor, Long targetUserId) {
        requireUser(targetUserId);
        return new FollowStatusDto(
                targetUserId,
                followMapper.existsFollow(actor.getUserId(), targetUserId) > 0,
                followMapper.countFollowers(targetUserId),
                followMapper.countFollowing(targetUserId)
        );
    }

    public PublicUserProfileDto profile(User actor, Long targetUserId, List<AiRouteDto> savedCourses, List<PostDto> posts) {
        User target = requireUser(targetUserId);
        boolean followedByMe = actor != null && !actor.getUserId().equals(targetUserId)
                && followMapper.existsFollow(actor.getUserId(), targetUserId) > 0;
        return new PublicUserProfileDto(
                UserDto.from(target),
                followMapper.countFollowers(targetUserId),
                followMapper.countFollowing(targetUserId),
                followedByMe,
                savedCourses == null ? List.of() : savedCourses,
                posts == null ? List.of() : posts
        );
    }

    private FollowUserDto toDto(User user) {
        return FollowUserDto.from(
                user,
                user.getFollowerCount(),
                user.getFollowingCount(),
                user.getFollowedByMe()
        );
    }

    private void requireTarget(User actor, Long targetUserId) {
        requireUser(targetUserId);
        if (actor.getUserId().equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot follow self.");
        }
    }

    private User requireUser(Long userId) {
        User user = userId == null ? null : userMapper.findById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user was not found.");
        }
        return user;
    }
}
