package com.hubnapse.service;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hubnapse.entity.PostEntity;
import com.hubnapse.entity.PostLikeEntity;
import com.hubnapse.entity.UserEntity;
import com.hubnapse.exception.AlreadyLikedException;
import com.hubnapse.exception.PostNotFoundException;
import com.hubnapse.exception.UserNotFoundException;
import com.hubnapse.repository.PostLikeRepository;
import com.hubnapse.repository.PostRepository;
import com.hubnapse.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public void like(Long postId, String userEmail) {

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        if (postLikeRepository.existsByPostIdAndUserId(postId, user.getId())) {
            throw new AlreadyLikedException(postId);
        }

        PostLikeEntity entity = new PostLikeEntity();

        entity.setPost(post);
        entity.setUser(user);
        entity.setCreatedAt(OffsetDateTime.now());

        try {
            postLikeRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyLikedException(postId);
        }
    }

    @Transactional
    public void unlike(Long postId, String userEmail) {

        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(postId);
        }

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        postLikeRepository.deleteByPostIdAndUserId(postId, user.getId());
    }

    public long countByPostId(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    public boolean isLikedBy(Long postId, Long userId) {

        if (userId == null) {
            return false;
        }

        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    public Map<Long, Long> countGroupedByPostIds(Collection<Long> postIds) {

        if (postIds.isEmpty()) {
            return Map.of();
        }

        return postLikeRepository.countGroupedByPostIds(postIds)
                .stream()
                .collect(Collectors.toMap(
                        PostLikeRepository.PostLikeCount::getPostId,
                        PostLikeRepository.PostLikeCount::getLikeCount));
    }

    public Set<Long> findLikedPostIds(Long userId, Collection<Long> postIds) {

        if (userId == null || postIds.isEmpty()) {
            return Set.of();
        }

        return new HashSet<>(postLikeRepository.findLikedPostIds(userId, postIds));
    }
}
