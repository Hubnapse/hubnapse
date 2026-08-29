package com.hubnapse.dto;

import java.time.OffsetDateTime;

public record CommentResponse(

        Long id,
        String content,
        PostAuthorResponse author,
        Long parentId,
        OffsetDateTime createdAt

) {
}
