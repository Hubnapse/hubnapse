package com.hubnapse.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.hubnapse.dto.PostAuthorResponse;
import com.hubnapse.dto.PostResponse;
import com.hubnapse.security.JsonAccessDeniedHandler;
import com.hubnapse.security.JsonAuthenticationEntryPoint;
import com.hubnapse.security.SecurityConfig;
import com.hubnapse.service.PostService;

@WebMvcTest(PostController.class)
@Import({ SecurityConfig.class, JsonAuthenticationEntryPoint.class, JsonAccessDeniedHandler.class })
class PostControllerSecurityTest {

    private static final String POST_BODY = """
            {"title":"タイトル","description":"説明","imageUrl":"https://example.com/a.png",
             "whatCreated":"イラスト","tips":"tips","bestPrompt":"prompt"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private PostResponse samplePostResponse() {

        PostAuthorResponse author = new PostAuthorResponse(1L, "taro", "太郎", null);

        return new PostResponse(
                10L, author, "タイトル", "説明", "https://example.com/a.png",
                "イラスト", "tips", "prompt", OffsetDateTime.now(), OffsetDateTime.now());
    }

    @Test
    void findAll_remainsPermitAllWithoutAuthentication() throws Exception {

        when(postService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk());
    }

    @Test
    void findById_remainsPermitAllWithoutAuthentication() throws Exception {

        when(postService.findById(10L)).thenReturn(samplePostResponse());

        mockMvc.perform(get("/api/posts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author.username").value("taro"));
    }

    @Test
    void create_withoutAuthentication_returns401() throws Exception {

        mockMvc.perform(post("/api/posts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(POST_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_withAuthentication_usesAuthenticatedUserAndReturns200() throws Exception {

        when(postService.create(any(), eq("taro@example.com"))).thenReturn(samplePostResponse());

        mockMvc.perform(post("/api/posts")
                .with(user("taro@example.com"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(POST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author.username").value("taro"));
    }

    @Test
    void update_withoutAuthentication_returns401() throws Exception {

        mockMvc.perform(put("/api/posts/10")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(POST_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_byOwner_returns200() throws Exception {

        when(postService.update(eq(10L), any(), eq("taro@example.com"))).thenReturn(samplePostResponse());

        mockMvc.perform(put("/api/posts/10")
                .with(user("taro@example.com"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(POST_BODY))
                .andExpect(status().isOk());
    }

    @Test
    void update_byNonOwner_returns403() throws Exception {

        when(postService.update(eq(10L), any(), eq("jiro@example.com")))
                .thenThrow(new AccessDeniedException("この投稿を編集・削除する権限がありません"));

        mockMvc.perform(put("/api/posts/10")
                .with(user("jiro@example.com"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(POST_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_withoutAuthentication_returns401() throws Exception {

        mockMvc.perform(delete("/api/posts/10").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_byOwner_returns204() throws Exception {

        mockMvc.perform(delete("/api/posts/10")
                .with(user("taro@example.com"))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_byNonOwner_returns403() throws Exception {

        doThrow(new AccessDeniedException("この投稿を編集・削除する権限がありません"))
                .when(postService).delete(anyLong(), eq("jiro@example.com"));

        mockMvc.perform(delete("/api/posts/10")
                .with(user("jiro@example.com"))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
