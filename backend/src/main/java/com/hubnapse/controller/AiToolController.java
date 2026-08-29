package com.hubnapse.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hubnapse.dto.AiToolResponse;
import com.hubnapse.service.AiToolService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai-tools")
@RequiredArgsConstructor
public class AiToolController {

    private final AiToolService aiToolService;

    @GetMapping
    public List<AiToolResponse> findAll() {
        return aiToolService.findAll();
    }
}
