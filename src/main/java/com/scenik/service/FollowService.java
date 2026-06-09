package com.scenik.service;

import com.scenik.domain.User;
import com.scenik.dto.FollowDtos.FollowStatusDto;
import com.scenik.dto.FollowDtos.FollowUserDto;
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
        return followMapper.findFollowers(userId).stream()
                .map(user -> toDto(actor, user))
                .toList();
    }

    public List<FollowUserDto> following(User actor, Long userId) {
        requireUser(userId);
        return followMapper.findFollowing(userId).stream()
                .map(user -> toDto(actor, user))
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

    private FollowUserDto toDto(User actor, User user) {
        return FollowUserDto.from(
                user,
                followMapper.countFollowers(user.getUserId()),
                followMapper.countFollowing(user.getUserId()),
                actor != null && followMapper.existsFollow(actor.getUserId(), user.getUserId()) > 0
        );
    }

    private void requireTarget(User actor, Long targetUserId) {
        requireUser(targetUserId);
        if (actor.getUserId().equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot follow self.");
        }
    }

    private void requireUser(Long userId) {
        if (userId == null || userMapper.findById(userId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user was not found.");
        }
    }
}
