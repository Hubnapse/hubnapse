package com.hubnapse.dto;

public record PostAuthorResponse(

        Long id,
        String username,
        String displayName,
        String iconUrl,
        boolean followedByCurrentUser

) {
}
