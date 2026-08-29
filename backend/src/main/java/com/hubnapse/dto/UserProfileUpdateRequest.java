package com.hubnapse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(

        @NotBlank @Size(max = 50) String displayName,

        @Size(max = 500) String iconUrl,

        @Size(max = 300) String bio

) {
}
