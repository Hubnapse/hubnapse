package com.hubnapse.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.hubnapse.dto.UserResponse;
import com.hubnapse.security.JsonAccessDeniedHandler;
import com.hubnapse.security.JsonAuthenticationEntryPoint;
import com.hubnapse.security.SecurityConfig;
import com.hubnapse.service.FollowService;
import com.hubnapse.service.UserService;

@WebMvcTest(UserController.class)
@Import({ SecurityConfig.class, JsonAuthenticationEntryPoint.class, JsonAccessDeniedHandler.class })
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private FollowService followService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void register_isPermitAllWithoutAuthentication() throws Exception {

        UserResponse response = new UserResponse(
                1L, "taro", "太郎", "taro@example.com", null, null,
                0L, 0L, false,
                OffsetDateTime.now(), OffsetDateTime.now());

        when(userService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"taro","displayName":"太郎","email":"taro@example.com","password":"password123"}
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void me_withoutAuthentication_returns401() throws Exception {

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_authenticated_returnsUserResponse() throws Exception {

        UserResponse response = new UserResponse(
                1L, "taro", "太郎", "taro@example.com", null, null,
                0L, 0L, false,
                OffsetDateTime.now(), OffsetDateTime.now());

        when(userService.getByEmail(eq("taro@example.com"))).thenReturn(response);

        mockMvc.perform(get("/api/users/me").with(user("taro@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("taro"))
                .andExpect(jsonPath("$.email").value("taro@example.com"));
    }

    @Test
    void follow_withoutAuthentication_returns401() throws Exception {

        mockMvc.perform(post("/api/users/2/follow").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void follow_withAuthentication_returns204() throws Exception {

        mockMvc.perform(post("/api/users/2/follow")
                .with(user("taro@example.com"))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void unfollow_withoutAuthentication_returns401() throws Exception {

        mockMvc.perform(delete("/api/users/2/follow").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unfollow_withAuthentication_returns204() throws Exception {

        mockMvc.perform(delete("/api/users/2/follow")
                .with(user("taro@example.com"))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
