package com.hubnapse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hubnapse.dto.AiToolResponse;
import com.hubnapse.dto.UserProfileResponse;
import com.hubnapse.dto.UserProfileUpdateRequest;
import com.hubnapse.entity.UserEntity;
import com.hubnapse.exception.UserNotFoundException;
import com.hubnapse.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FollowService followService;

    @Mock
    private AiToolService aiToolService;

    @InjectMocks
    private UserService userService;

    private UserEntity user;

    @BeforeEach
    void setUp() {

        user = new UserEntity();
        user.setId(1L);
        user.setUsername("taro");
        user.setDisplayName("太郎");
        user.setEmail("taro@example.com");
        user.setIconUrl("https://example.com/taro.png");
        user.setBio("元のbio");
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void getProfileByUsername_success() {

        when(userRepository.findByUsername("taro")).thenReturn(Optional.of(user));
        when(followService.getFollowerCount(1L)).thenReturn(3L);
        when(followService.getFollowingCount(1L)).thenReturn(5L);
        when(aiToolService.findByUserId(1L)).thenReturn(List.of(new AiToolResponse(1L, "ChatGPT")));

        UserProfileResponse response = userService.getProfileByUsername("taro", null);

        assertThat(response.username()).isEqualTo("taro");
        assertThat(response.displayName()).isEqualTo("太郎");
        assertThat(response.followerCount()).isEqualTo(3L);
        assertThat(response.followingCount()).isEqualTo(5L);
        assertThat(response.followedByCurrentUser()).isFalse();
        assertThat(response.aiTools()).hasSize(1);
    }

    @Test
    void getProfileByUsername_whenLoggedInViewer_resolvesFollowStatus() {

        when(userRepository.findByUsername("taro")).thenReturn(Optional.of(user));
        when(userRepository.findIdByEmail("jiro@example.com")).thenReturn(Optional.of(2L));
        when(followService.isFollowing(2L, 1L)).thenReturn(true);

        UserProfileResponse response = userService.getProfileByUsername("taro", "jiro@example.com");

        assertThat(response.followedByCurrentUser()).isTrue();
    }

    @Test
    void getProfileByUsername_whenNotFound_throwsUserNotFoundException() {

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfileByUsername("unknown", null))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateProfile_updatesDisplayNameIconUrlBio_butNotUsername() {

        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                "新しい太郎", "https://example.com/new.png", "新しいbio");

        var response = userService.updateProfile(request, "taro@example.com");

        assertThat(response.displayName()).isEqualTo("新しい太郎");
        assertThat(response.iconUrl()).isEqualTo("https://example.com/new.png");
        assertThat(response.bio()).isEqualTo("新しいbio");
        assertThat(response.username()).isEqualTo("taro");
    }

    @Test
    void updateProfile_whenUserNotFound_throwsUserNotFoundException() {

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        UserProfileUpdateRequest request = new UserProfileUpdateRequest("x", null, null);

        assertThatThrownBy(() -> userService.updateProfile(request, "unknown@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }
}
