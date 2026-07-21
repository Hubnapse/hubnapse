package com.hubnapse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostRequest(

        @NotBlank @Size(max = 100) String title,

        @Size(max = 500) String description,

        @NotBlank String imageUrl,

        @NotBlank String whatCreated,

        String tips,

        String bestPrompt

) {
}