package com.hubnapse.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.hubnapse.security.JsonAccessDeniedHandler;
import com.hubnapse.security.JsonAuthenticationEntryPoint;
import com.hubnapse.security.SecurityConfig;
import com.hubnapse.service.PostService;

@WebMvcTest(PostController.class)
@Import({ SecurityConfig.class, JsonAuthenticationEntryPoint.class, JsonAccessDeniedHandler.class })
class PostControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void findAll_remainsPermitAllWithoutAuthentication() throws Exception {

        when(postService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk());
    }
}
