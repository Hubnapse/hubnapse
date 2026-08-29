package com.hubnapse.dto;

public record CsrfTokenResponse(

        String parameterName,
        String headerName,
        String token

) {
}
