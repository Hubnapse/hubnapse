package com.hubnapse.service;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hubnapse.entity.UserEntity;
import com.hubnapse.entity.UserFollowEntity;
import com.hubnapse.exception.AlreadyFollowingException;
import com.hubnapse.exception.SelfFollowException;
import com.hubnapse.exception.UserNotFoundException;
import com.hubnapse.repository.UserFollowRepository;
import com.hubnapse.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;

    public void follow(Long targetUserId, String followerEmail) {

        UserEntity follower = userRepository.findByEmail(followerEmail)
                .orElseThrow(() -> new UserNotFoundException(followerEmail));

        UserEntity following = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException(targetUserId));

        if (follower.getId().equals(following.getId())) {
            throw new SelfFollowException();
        }

        if (userFollowRepository.existsByFollowerIdAndFollowingId(follower.getId(), following.getId())) {
            throw new AlreadyFollowingException(following.getId());
        }

        UserFollowEntity entity = new UserFollowEntity();

        entity.setFollower(follower);
        entity.setFollowing(following);
        entity.setCreatedAt(OffsetDateTime.now());

        try {
            userFollowRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyFollowingException(following.getId());
        }
    }

    @Transactional
    public void unfollow(Long targetUserId, String followerEmail) {

        UserEntity follower = userRepository.findByEmail(followerEmail)
                .orElseThrow(() -> new UserNotFoundException(followerEmail));

        if (!userRepository.existsById(targetUserId)) {
            throw new UserNotFoundException(targetUserId);
        }

        userFollowRepository.deleteByFollowerIdAndFollowingId(follower.getId(), targetUserId);
    }

    public long getFollowerCount(Long userId) {
        return userFollowRepository.countByFollowingId(userId);
    }

    public long getFollowingCount(Long userId) {
        return userFollowRepository.countByFollowerId(userId);
    }

    public boolean isFollowing(Long followerId, Long targetUserId) {

        if (followerId == null) {
            return false;
        }

        return userFollowRepository.existsByFollowerIdAndFollowingId(followerId, targetUserId);
    }

    public Set<Long> findFollowingIds(Long followerId, Collection<Long> candidateUserIds) {

        if (followerId == null || candidateUserIds.isEmpty()) {
            return Set.of();
        }

        return new HashSet<>(userFollowRepository.findFollowingIdsAmong(followerId, candidateUserIds));
    }
}
