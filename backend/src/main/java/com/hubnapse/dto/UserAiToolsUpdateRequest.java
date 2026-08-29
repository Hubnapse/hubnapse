package com.hubnapse.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record UserAiToolsUpdateRequest(

        @NotNull List<Long> aiToolIds

) {
}
