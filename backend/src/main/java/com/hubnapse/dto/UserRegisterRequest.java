package com.hubnapse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(

        @NotBlank
        @Size(min = 3, max = 30)
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "usernameは半角英数字とアンダースコアのみ使用できます")
        String username,

        @NotBlank @Size(max = 50) String displayName,

        @NotBlank @Email String email,

        @NotBlank @Size(min = 8, max = 72) String password,

        @Size(max = 500) String iconUrl,

        @Size(max = 300) String bio

) {
}
