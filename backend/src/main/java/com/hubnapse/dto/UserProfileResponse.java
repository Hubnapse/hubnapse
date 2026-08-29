package com.hubnapse.dto;

import java.util.List;

public record UserProfileResponse(

        Long id,
        String username,
        String displayName,
        String iconUrl,
        String bio,
        long followerCount,
        long followingCount,
        boolean followedByCurrentUser,
        List<AiToolResponse> aiTools

) {
}
