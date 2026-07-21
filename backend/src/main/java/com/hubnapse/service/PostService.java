package com.hubnapse.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hubnapse.dto.PostRequest;
import com.hubnapse.dto.PostResponse;
import com.hubnapse.entity.PostEntity;
import com.hubnapse.exception.PostNotFoundException;
import com.hubnapse.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public List<PostResponse> findAll() {
        return postRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PostResponse create(PostRequest request) {

        PostEntity postEntity = new PostEntity();

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

    public PostResponse update(Long id, PostRequest request) {

        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

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

    public void delete(Long id) {

        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        postRepository.delete(postEntity);
    }

    public PostResponse findById(Long id) {

        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        return toResponse(postEntity);
    }

    private PostResponse toResponse(PostEntity entity) {

        return new PostResponse(
                entity.getId(),
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