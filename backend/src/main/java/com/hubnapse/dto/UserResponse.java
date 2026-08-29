package com.hubnapse.dto;

import java.time.OffsetDateTime;

public record UserResponse(

        Long id,
        String username,
        String displayName,
        String email,
        String iconUrl,
        String bio,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {
}
