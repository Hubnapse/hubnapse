package com.hubnapse.dto;

import java.time.OffsetDateTime;

public record UserResponse(

        Long id,
        String username,
        String displayName,
        String email,
        String iconUrl,
        String bio,
        long followerCount,
        long followingCount,
        boolean followedByCurrentUser,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {
}
