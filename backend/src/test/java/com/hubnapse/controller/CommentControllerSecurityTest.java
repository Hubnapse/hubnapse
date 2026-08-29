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

import com.hubnapse.dto.CommentResponse;
import com.hubnapse.dto.PostAuthorResponse;
import com.hubnapse.security.JsonAccessDeniedHandler;
import com.hubnapse.security.JsonAuthenticationEntryPoint;
import com.hubnapse.security.SecurityConfig;
import com.hubnapse.service.CommentService;

@WebMvcTest(CommentController.class)
@Import({ SecurityConfig.class, JsonAuthenticationEntryPoint.class, JsonAccessDeniedHandler.class })
class CommentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private CommentResponse sampleCommentResponse() {

        PostAuthorResponse author = new PostAuthorResponse(1L, "taro", "太郎", null, false);

        return new CommentResponse(10L, "コメント本文", author, null, OffsetDateTime.now());
    }

    @Test
    void findByPostId_permitAllWithoutAuthentication() throws Exception {

        when(commentService.findByPostId(100L, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/posts/100/comments"))
                .andExpect(status().isOk());
    }

    @Test
    void create_withoutAuthentication_returns401() throws Exception {

        mockMvc.perform(post("/api/posts/100/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"content":"コメント","parentId":null}
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_withAuthentication_returns200() throws Exception {

        when(commentService.create(eq(100L), any(), eq("taro@example.com")))
                .thenReturn(sampleCommentResponse());

        mockMvc.perform(post("/api/posts/100/comments")
                .with(user("taro@example.com"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"content":"コメント","parentId":null}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author.username").value("taro"));
    }

    @Test
    void create_withBlankContent_returns400() throws Exception {

        mockMvc.perform(post("/api/posts/100/comments")
                .with(user("taro@example.com"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"content":"","parentId":null}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_withoutAuthentication_returns401() throws Exception {

        mockMvc.perform(delete("/api/comments/10").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_byOwner_returns204() throws Exception {

        mockMvc.perform(delete("/api/comments/10")
                .with(user("taro@example.com"))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_byNonOwner_returns403() throws Exception {

        doThrow(new AccessDeniedException("このコメントを削除する権限がありません"))
                .when(commentService).delete(anyLong(), eq("jiro@example.com"));

        mockMvc.perform(delete("/api/comments/10")
                .with(user("jiro@example.com"))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
