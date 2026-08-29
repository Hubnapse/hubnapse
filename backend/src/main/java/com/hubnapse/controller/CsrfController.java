package com.hubnapse.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hubnapse.dto.CsrfTokenResponse;

@RestController
public class CsrfController {

    @GetMapping("/api/csrf")
    public CsrfTokenResponse csrf(CsrfToken csrfToken) {
        return new CsrfTokenResponse(
                csrfToken.getParameterName(),
                csrfToken.getHeaderName(),
                csrfToken.getToken());
    }
}
