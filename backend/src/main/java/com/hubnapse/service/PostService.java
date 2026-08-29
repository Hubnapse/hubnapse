package com.hubnapse.service;

import java.time.OffsetDateTime;
import java.util.List;

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

    public List<PostResponse> findAll() {
        return postRepository.findAll()
                .stream()
                .map(this::toResponse)
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

        return toResponse(savedEntity);
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

        return toResponse(updatedEntity);
    }

    public void delete(Long id, String requesterEmail) {

        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        requireOwner(postEntity, requesterEmail);

        postRepository.delete(postEntity);
    }

    public PostResponse findById(Long id) {

        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        return toResponse(postEntity);
    }

    private void requireOwner(PostEntity postEntity, String requesterEmail) {

        if (!postEntity.getAuthor().getEmail().equals(requesterEmail)) {
            throw new AccessDeniedException("この投稿を編集・削除する権限がありません");
        }
    }

    private PostResponse toResponse(PostEntity entity) {

        UserEntity author = entity.getAuthor();

        PostAuthorResponse authorResponse = new PostAuthorResponse(
                author.getId(),
                author.getUsername(),
                author.getDisplayName(),
                author.getIconUrl());

        return new PostResponse(
                entity.getId(),
                authorResponse,
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
