package com.hubnapse.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.hubnapse.dto.AiToolResponse;
import com.hubnapse.security.JsonAccessDeniedHandler;
import com.hubnapse.security.JsonAuthenticationEntryPoint;
import com.hubnapse.security.SecurityConfig;
import com.hubnapse.service.AiToolService;

@WebMvcTest(AiToolController.class)
@Import({ SecurityConfig.class, JsonAuthenticationEntryPoint.class, JsonAccessDeniedHandler.class })
class AiToolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiToolService aiToolService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void findAll_permitAllWithoutAuthentication() throws Exception {

        when(aiToolService.findAll()).thenReturn(List.of(new AiToolResponse(1L, "ChatGPT")));

        mockMvc.perform(get("/api/ai-tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ChatGPT"));
    }
}
