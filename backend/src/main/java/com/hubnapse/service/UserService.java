package com.hubnapse.service;

import java.time.OffsetDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hubnapse.dto.UserRegisterRequest;
import com.hubnapse.dto.UserResponse;
import com.hubnapse.entity.UserEntity;
import com.hubnapse.exception.DuplicateEmailException;
import com.hubnapse.exception.DuplicateUsernameException;
import com.hubnapse.exception.UserNotFoundException;
import com.hubnapse.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FollowService followService;

    public UserResponse register(UserRegisterRequest request) {

        String normalizedEmail = request.email().toLowerCase();

        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUsernameException(request.username());
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        UserEntity userEntity = new UserEntity();

        userEntity.setUsername(request.username());
        userEntity.setDisplayName(request.displayName());
        userEntity.setEmail(normalizedEmail);
        userEntity.setPasswordHash(passwordEncoder.encode(request.password()));
        userEntity.setIconUrl(request.iconUrl());
        userEntity.setBio(request.bio());

        OffsetDateTime now = OffsetDateTime.now();

        userEntity.setCreatedAt(now);
        userEntity.setUpdatedAt(now);

        UserEntity savedEntity = userRepository.save(userEntity);

        return toResponse(savedEntity);
    }

    public UserResponse getByEmail(String email) {

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        return toResponse(userEntity);
    }

    private UserResponse toResponse(UserEntity entity) {

        long followerCount = followService.getFollowerCount(entity.getId());
        long followingCount = followService.getFollowingCount(entity.getId());

        return new UserResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getDisplayName(),
                entity.getEmail(),
                entity.getIconUrl(),
                entity.getBio(),
                followerCount,
                followingCount,
                // UserResponseは現状常に本人のプロフィールとしてのみ返されるため、自己フォローは常にfalse
                false,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
