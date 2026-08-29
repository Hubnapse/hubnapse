package com.hubnapse.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@WebMvcTest(CsrfController.class)
@Import({ SecurityConfig.class, JsonAuthenticationEntryPoint.class, JsonAccessDeniedHandler.class })
class CsrfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void csrf_isPermitAllAndReturnsTokenFields() throws Exception {

        mockMvc.perform(get("/api/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parameterName").exists())
                .andExpect(jsonPath("$.headerName").exists())
                .andExpect(jsonPath("$.token").exists());
    }
}
