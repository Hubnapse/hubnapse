package com.hubnapse.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.hubnapse.dto.CommentRequest;
import com.hubnapse.dto.CommentResponse;
import com.hubnapse.dto.PostAuthorResponse;
import com.hubnapse.entity.CommentEntity;
import com.hubnapse.entity.PostEntity;
import com.hubnapse.entity.UserEntity;
import com.hubnapse.exception.CommentNotFoundException;
import com.hubnapse.exception.InvalidParentCommentException;
import com.hubnapse.exception.PostNotFoundException;
import com.hubnapse.exception.UserNotFoundException;
import com.hubnapse.repository.CommentRepository;
import com.hubnapse.repository.PostRepository;
import com.hubnapse.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FollowService followService;

    public List<CommentResponse> findByPostId(Long postId, String viewerEmail) {

        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(postId);
        }

        List<CommentEntity> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        if (comments.isEmpty()) {
            return List.of();
        }

        List<Long> authorIds = comments.stream()
                .map(comment -> comment.getAuthor().getId())
                .distinct()
                .toList();

        Long viewerId = resolveViewerId(viewerEmail);
        Set<Long> followedAuthorIds = followService.findFollowingIds(viewerId, authorIds);

        return comments.stream()
                .map(comment -> toResponse(comment, followedAuthorIds.contains(comment.getAuthor().getId())))
                .toList();
    }

    public CommentResponse create(Long postId, CommentRequest request, String authorEmail) {

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        UserEntity author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new UserNotFoundException(authorEmail));

        CommentEntity parent = null;

        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new CommentNotFoundException(request.parentId()));

            if (!parent.getPost().getId().equals(postId)) {
                throw new InvalidParentCommentException(request.parentId(), postId);
            }
        }

        CommentEntity commentEntity = new CommentEntity();

        commentEntity.setPost(post);
        commentEntity.setAuthor(author);
        commentEntity.setParent(parent);
        commentEntity.setContent(request.content());
        commentEntity.setCreatedAt(OffsetDateTime.now());

        CommentEntity savedEntity = commentRepository.save(commentEntity);

        // 投稿者は自分自身のため、著者のフォロー状態は必ずfalse
        return toResponse(savedEntity, false);
    }

    public void delete(Long commentId, String requesterEmail) {

        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!commentEntity.getAuthor().getEmail().equals(requesterEmail)) {
            throw new AccessDeniedException("このコメントを削除する権限がありません");
        }

        commentRepository.delete(commentEntity);
    }

    private Long resolveViewerId(String viewerEmail) {

        if (viewerEmail == null) {
            return null;
        }

        return userRepository.findIdByEmail(viewerEmail).orElse(null);
    }

    private CommentResponse toResponse(CommentEntity entity, boolean authorFollowedByCurrentUser) {

        UserEntity author = entity.getAuthor();

        PostAuthorResponse authorResponse = new PostAuthorResponse(
                author.getId(),
                author.getUsername(),
                author.getDisplayName(),
                author.getIconUrl(),
                authorFollowedByCurrentUser);

        Long parentId = entity.getParent() != null ? entity.getParent().getId() : null;

        return new CommentResponse(
                entity.getId(),
                entity.getContent(),
                authorResponse,
                parentId,
                entity.getCreatedAt());
    }
}
