package com.hubnapse.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.hubnapse.dto.UserResponse;
import com.hubnapse.security.JsonAccessDeniedHandler;
import com.hubnapse.security.JsonAuthenticationEntryPoint;
import com.hubnapse.security.SecurityConfig;
import com.hubnapse.service.AuthService;

@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, JsonAuthenticationEntryPoint.class, JsonAccessDeniedHandler.class })
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void login_success_returns200AndUser() throws Exception {

        UserResponse response = new UserResponse(
                1L, "taro", "太郎", "taro@example.com", null, null,
                OffsetDateTime.now(), OffsetDateTime.now());

        when(authService.login(any(), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"taro@example.com","password":"password123"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("taro@example.com"));
    }

    @Test
    void login_badCredentials_returns401() throws Exception {

        when(authService.login(any(), any(), any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"taro@example.com","password":"wrong"}
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void login_withoutCsrfToken_returns403() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"taro@example.com","password":"password123"}
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_invalidBody_returns400() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"","password":""}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logout_withoutAuthentication_returns401() throws Exception {

        mockMvc.perform(post("/api/auth/logout").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_withoutCsrfToken_returns403() throws Exception {

        mockMvc.perform(post("/api/auth/logout").with(user("taro@example.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void logout_authenticated_returns204() throws Exception {

        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .with(user("taro@example.com")))
                .andExpect(status().isNoContent());
    }
}
