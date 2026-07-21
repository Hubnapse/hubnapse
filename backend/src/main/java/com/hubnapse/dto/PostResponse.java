package com.hubnapse.dto;

import java.time.OffsetDateTime;

public record PostResponse(

                Long id,
                String title,
                String description,
                String imageUrl,
                String whatCreated,
                String tips,
                String bestPrompt,
                OffsetDateTime createdAt,
                OffsetDateTime updatedAt

) {
}