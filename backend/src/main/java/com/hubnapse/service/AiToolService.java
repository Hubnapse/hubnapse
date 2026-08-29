package com.hubnapse.service;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hubnapse.dto.AiToolResponse;
import com.hubnapse.entity.AiToolEntity;
import com.hubnapse.entity.UserAiToolEntity;
import com.hubnapse.entity.UserEntity;
import com.hubnapse.exception.AiToolNotFoundException;
import com.hubnapse.exception.UserNotFoundException;
import com.hubnapse.repository.AiToolRepository;
import com.hubnapse.repository.UserAiToolRepository;
import com.hubnapse.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiToolService {

    private final AiToolRepository aiToolRepository;
    private final UserAiToolRepository userAiToolRepository;
    private final UserRepository userRepository;

    public List<AiToolResponse> findAll() {
        return aiToolRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AiToolResponse> findByUserId(Long userId) {
        return userAiToolRepository.findByUserId(userId)
                .stream()
                .map(entity -> toResponse(entity.getAiTool()))
                .toList();
    }

    @Transactional
    public List<AiToolResponse> updateSelection(String userEmail, List<Long> aiToolIds) {

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        Set<Long> distinctIds = new LinkedHashSet<>(aiToolIds);

        List<AiToolEntity> aiTools = aiToolRepository.findAllById(distinctIds);

        if (aiTools.size() != distinctIds.size()) {
            Set<Long> foundIds = aiTools.stream().map(AiToolEntity::getId).collect(Collectors.toSet());
            Long missingId = distinctIds.stream().filter(id -> !foundIds.contains(id)).findFirst().orElseThrow();
            throw new AiToolNotFoundException(missingId);
        }

        userAiToolRepository.deleteByUserId(user.getId());

        OffsetDateTime now = OffsetDateTime.now();

        List<UserAiToolEntity> entities = aiTools.stream()
                .map(tool -> {
                    UserAiToolEntity entity = new UserAiToolEntity();
                    entity.setUser(user);
                    entity.setAiTool(tool);
                    entity.setCreatedAt(now);
                    return entity;
                })
                .toList();

        userAiToolRepository.saveAll(entities);

        return aiTools.stream().map(this::toResponse).toList();
    }

    private AiToolResponse toResponse(AiToolEntity entity) {
        return new AiToolResponse(entity.getId(), entity.getName());
    }
}
