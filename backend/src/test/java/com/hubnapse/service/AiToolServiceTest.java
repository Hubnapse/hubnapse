package com.hubnapse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hubnapse.dto.AiToolResponse;
import com.hubnapse.entity.AiToolEntity;
import com.hubnapse.entity.UserAiToolEntity;
import com.hubnapse.entity.UserEntity;
import com.hubnapse.exception.AiToolNotFoundException;
import com.hubnapse.repository.AiToolRepository;
import com.hubnapse.repository.UserAiToolRepository;
import com.hubnapse.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AiToolServiceTest {

    @Mock
    private AiToolRepository aiToolRepository;

    @Mock
    private UserAiToolRepository userAiToolRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AiToolService aiToolService;

    private UserEntity user;
    private AiToolEntity chatGpt;
    private AiToolEntity claude;

    @BeforeEach
    void setUp() {

        user = new UserEntity();
        user.setId(1L);
        user.setEmail("taro@example.com");

        chatGpt = new AiToolEntity();
        chatGpt.setId(1L);
        chatGpt.setName("ChatGPT");

        claude = new AiToolEntity();
        claude.setId(2L);
        claude.setName("Claude");
    }

    @Test
    void findAll_returnsMasterList() {

        when(aiToolRepository.findAllByOrderByIdAsc()).thenReturn(List.of(chatGpt, claude));

        List<AiToolResponse> responses = aiToolService.findAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("ChatGPT");
    }

    @Test
    void findByUserId_returnsSelectedTools() {

        UserAiToolEntity link = new UserAiToolEntity();
        link.setUser(user);
        link.setAiTool(chatGpt);

        when(userAiToolRepository.findByUserId(1L)).thenReturn(List.of(link));

        List<AiToolResponse> responses = aiToolService.findByUserId(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("ChatGPT");
    }

    @Test
    void updateSelection_replacesExistingSelection() {

        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(user));
        when(aiToolRepository.findAllById(anyCollection())).thenReturn(List.of(chatGpt, claude));

        List<AiToolResponse> responses = aiToolService.updateSelection("taro@example.com", List.of(1L, 2L));

        verify(userAiToolRepository).deleteByUserId(1L);
        verify(userAiToolRepository).saveAll(any());
        assertThat(responses).hasSize(2);
    }

    @Test
    void updateSelection_deduplicatesRequestedIds() {

        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(user));
        when(aiToolRepository.findAllById(anyCollection())).thenReturn(List.of(chatGpt));

        List<AiToolResponse> responses = aiToolService.updateSelection("taro@example.com", List.of(1L, 1L, 1L));

        assertThat(responses).hasSize(1);
    }

    @Test
    void updateSelection_whenIdDoesNotExist_throwsAiToolNotFoundException() {

        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(user));
        when(aiToolRepository.findAllById(anyCollection())).thenReturn(List.of(chatGpt));

        assertThatThrownBy(() -> aiToolService.updateSelection("taro@example.com", List.of(1L, 999L)))
                .isInstanceOf(AiToolNotFoundException.class);

        verify(userAiToolRepository, never()).deleteByUserId(any());
    }

    @Test
    void updateSelection_withEmptyList_clearsSelection() {

        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(user));
        when(aiToolRepository.findAllById(anyCollection())).thenReturn(List.of());

        List<AiToolResponse> responses = aiToolService.updateSelection("taro@example.com", List.of());

        verify(userAiToolRepository).deleteByUserId(1L);
        assertThat(responses).isEmpty();
    }
}
