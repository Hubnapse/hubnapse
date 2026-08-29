package com.hubnapse.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.hubnapse.dto.UserProfileResponse;
import com.hubnapse.dto.UserResponse;
import com.hubnapse.exception.UserNotFoundException;
import com.hubnapse.security.JsonAccessDeniedHandler;
import com.hubnapse.security.JsonAuthenticationEntryPoint;
import com.hubnapse.security.SecurityConfig;
import com.hubnapse.service.AiToolService;
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
    private AiToolService aiToolService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UserResponse sampleUserResponse() {
        return new UserResponse(
                1L, "taro", "太郎", "taro@example.com", null, null,
                0L, 0L, false,
                OffsetDateTime.now(), OffsetDateTime.now());
    }

    @Test
    void register_isPermitAllWithoutAuthentication() throws Exception {

        when(userService.register(any())).thenReturn(sampleUserResponse());

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

        when(userService.getByEmail(eq("taro@example.com"))).thenReturn(sampleUserResponse());

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

    @Test
    void getProfile_permitAllWithoutAuthentication() throws Exception {

        UserProfileResponse profile = new UserProfileResponse(
                1L, "taro", "太郎", null, "bio", 3L, 5L, false, List.of());

        when(userService.getProfileByUsername(eq("taro"), any())).thenReturn(profile);

        mockMvc.perform(get("/api/users/taro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("taro"))
                .andExpect(jsonPath("$.followerCount").value(3))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void getProfile_whenNotFound_returns404() throws Exception {

        when(userService.getProfileByUsername(eq("unknown"), any()))
                .thenThrow(new UserNotFoundException("username", "unknown"));

        mockMvc.perform(get("/api/users/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProfile_withoutAuthentication_returns401() throws Exception {

        mockMvc.perform(put("/api/users/me")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"displayName":"新しい太郎","iconUrl":null,"bio":null}
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_withAuthentication_returns200() throws Exception {

        when(userService.updateProfile(any(), eq("taro@example.com"))).thenReturn(sampleUserResponse());

        mockMvc.perform(put("/api/users/me")
                .with(user("taro@example.com"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"displayName":"新しい太郎","iconUrl":null,"bio":null}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("taro"));
    }

    @Test
    void updateProfile_ignoresUsernameFieldInRequestBody() throws Exception {

        when(userService.updateProfile(any(), eq("taro@example.com"))).thenReturn(sampleUserResponse());

        mockMvc.perform(put("/api/users/me")
                .with(user("taro@example.com"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"hacker","displayName":"新しい太郎","iconUrl":null,"bio":null}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("taro"));
    }

    @Test
    void updateAiTools_withoutAuthentication_returns401() throws Exception {

        mockMvc.perform(put("/api/users/me/ai-tools")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"aiToolIds":[1,2]}
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateAiTools_withAuthentication_returns200() throws Exception {

        when(aiToolService.updateSelection(eq("taro@example.com"), any())).thenReturn(List.of());

        mockMvc.perform(put("/api/users/me/ai-tools")
                .with(user("taro@example.com"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"aiToolIds":[1,2]}
                        """))
                .andExpect(status().isOk());
    }
}
