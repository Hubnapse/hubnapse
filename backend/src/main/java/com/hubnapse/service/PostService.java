package com.hubnapse.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.hubnapse.dto.PostAuthorResponse;
import com.hubnapse.dto.PostRequest;
import com.hubnapse.dto.PostResponse;
import com.hubnapse.entity.PostEntity;
import com.hubnapse.entity.UserEntity;
import com.hubnapse.exception.PostNotFoundException;
import com.hubnapse.exception.UserNotFoundException;
import com.hubnapse.repository.PostRepository;
import com.hubnapse.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeService postLikeService;
    private final FollowService followService;

    public List<PostResponse> findAll(String viewerEmail) {

        List<PostEntity> postEntities = postRepository.findAll();

        if (postEntities.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = postEntities.stream().map(PostEntity::getId).toList();
        List<Long> authorIds = postEntities.stream()
                .map(entity -> entity.getAuthor().getId())
                .distinct()
                .toList();

        Map<Long, Long> likeCounts = postLikeService.countGroupedByPostIds(postIds);

        Long viewerId = resolveViewerId(viewerEmail);
        Set<Long> likedPostIds = postLikeService.findLikedPostIds(viewerId, postIds);
        Set<Long> followedAuthorIds = followService.findFollowingIds(viewerId, authorIds);

        return postEntities.stream()
                .map(entity -> toResponse(
                        entity,
                        likeCounts.getOrDefault(entity.getId(), 0L),
                        likedPostIds.contains(entity.getId()),
                        followedAuthorIds.contains(entity.getAuthor().getId())))
                .toList();
    }

    public PostResponse create(PostRequest request, String authorEmail) {

        UserEntity author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new UserNotFoundException(authorEmail));

        PostEntity postEntity = new PostEntity();

        postEntity.setAuthor(author);
        postEntity.setTitle(request.title());
        postEntity.setDescription(request.description());
        postEntity.setImageUrl(request.imageUrl());
        postEntity.setWhatCreated(request.whatCreated());
        postEntity.setTips(request.tips());
        postEntity.setBestPrompt(request.bestPrompt());

        OffsetDateTime now = OffsetDateTime.now();

        postEntity.setCreatedAt(now);
        postEntity.setUpdatedAt(now);

        PostEntity savedEntity = postRepository.save(postEntity);

        // 作成直後のためいいねは必ず0件、著者は自分自身のためフォロー状態は必ずfalse
        return toResponse(savedEntity, 0L, false, false);
    }

    public PostResponse update(Long id, PostRequest request, String requesterEmail) {

        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        requireOwner(postEntity, requesterEmail);

        postEntity.setTitle(request.title());
        postEntity.setDescription(request.description());
        postEntity.setImageUrl(request.imageUrl());
        postEntity.setWhatCreated(request.whatCreated());
        postEntity.setTips(request.tips());
        postEntity.setBestPrompt(request.bestPrompt());

        postEntity.setUpdatedAt(OffsetDateTime.now());

        PostEntity updatedEntity = postRepository.save(postEntity);

        Long authorId = updatedEntity.getAuthor().getId();
        long likeCount = postLikeService.countByPostId(id);
        boolean likedByOwner = postLikeService.isLikedBy(id, authorId);

        // 更新できるのは投稿者本人のみのため、著者のフォロー状態(自分自身)は必ずfalse
        return toResponse(updatedEntity, likeCount, likedByOwner, false);
    }

    public void delete(Long id, String requesterEmail) {

        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        requireOwner(postEntity, requesterEmail);

        postRepository.delete(postEntity);
    }

    public PostResponse findById(Long id, String viewerEmail) {

        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        Long viewerId = resolveViewerId(viewerEmail);

        long likeCount = postLikeService.countByPostId(id);
        boolean liked = postLikeService.isLikedBy(id, viewerId);
        boolean followedAuthor = followService.isFollowing(viewerId, postEntity.getAuthor().getId());

        return toResponse(postEntity, likeCount, liked, followedAuthor);
    }

    private Long resolveViewerId(String viewerEmail) {

        if (viewerEmail == null) {
            return null;
        }

        return userRepository.findIdByEmail(viewerEmail).orElse(null);
    }

    private void requireOwner(PostEntity postEntity, String requesterEmail) {

        if (!postEntity.getAuthor().getEmail().equals(requesterEmail)) {
            throw new AccessDeniedException("この投稿を編集・削除する権限がありません");
        }
    }

    private PostResponse toResponse(
            PostEntity entity,
            long likeCount,
            boolean likedByCurrentUser,
            boolean authorFollowedByCurrentUser) {

        UserEntity author = entity.getAuthor();

        PostAuthorResponse authorResponse = new PostAuthorResponse(
                author.getId(),
                author.getUsername(),
                author.getDisplayName(),
                author.getIconUrl(),
                authorFollowedByCurrentUser);

        return new PostResponse(
                entity.getId(),
                authorResponse,
                likeCount,
                likedByCurrentUser,
                entity.getTitle(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getWhatCreated(),
                entity.getTips(),
                entity.getBestPrompt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
