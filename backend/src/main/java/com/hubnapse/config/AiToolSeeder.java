package com.hubnapse.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.hubnapse.entity.AiToolEntity;
import com.hubnapse.repository.AiToolRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AiToolSeeder implements ApplicationRunner {

    private static final List<String> INITIAL_AI_TOOLS = List.of(
            "ChatGPT", "Claude", "Gemini", "GitHub Copilot");

    private final AiToolRepository aiToolRepository;

    @Override
    public void run(ApplicationArguments args) {

        for (String name : INITIAL_AI_TOOLS) {
            if (!aiToolRepository.existsByName(name)) {
                AiToolEntity entity = new AiToolEntity();
                entity.setName(name);
                aiToolRepository.save(entity);
            }
        }
    }
}
