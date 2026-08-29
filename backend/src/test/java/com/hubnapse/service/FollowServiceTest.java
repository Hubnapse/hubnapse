package com.hubnapse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hubnapse.entity.UserEntity;
import com.hubnapse.entity.UserFollowEntity;
import com.hubnapse.exception.AlreadyFollowingException;
import com.hubnapse.exception.SelfFollowException;
import com.hubnapse.exception.UserNotFoundException;
import com.hubnapse.repository.UserFollowRepository;
import com.hubnapse.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowService followService;

    private UserEntity follower;
    private UserEntity target;

    @BeforeEach
    void setUp() {

        follower = new UserEntity();
        follower.setId(1L);
        follower.setEmail("taro@example.com");

        target = new UserEntity();
        target.setId(2L);
        target.setEmail("jiro@example.com");
    }

    @Test
    void follow_success() {

        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userFollowRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);

        followService.follow(2L, "taro@example.com");

        verify(userFollowRepository).save(any(UserFollowEntity.class));
    }

    @Test
    void follow_duplicate_throwsAlreadyFollowingException() {

        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userFollowRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> followService.follow(2L, "taro@example.com"))
                .isInstanceOf(AlreadyFollowingException.class);

        verify(userFollowRepository, never()).save(any());
    }

    @Test
    void follow_self_throwsSelfFollowException() {

        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(follower));
        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));

        assertThatThrownBy(() -> followService.follow(1L, "taro@example.com"))
                .isInstanceOf(SelfFollowException.class);

        verify(userFollowRepository, never()).save(any());
    }

    @Test
    void follow_whenTargetNotFound_throwsUserNotFoundException() {

        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(follower));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followService.follow(999L, "taro@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void unfollow_success() {

        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(follower));
        when(userRepository.existsById(2L)).thenReturn(true);

        followService.unfollow(2L, "taro@example.com");

        verify(userFollowRepository).deleteByFollowerIdAndFollowingId(1L, 2L);
    }

    @Test
    void unfollow_whenNotFollowing_isIdempotentAndDoesNotThrow() {

        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(follower));
        when(userRepository.existsById(2L)).thenReturn(true);

        followService.unfollow(2L, "taro@example.com");

        verify(userFollowRepository).deleteByFollowerIdAndFollowingId(1L, 2L);
    }

    @Test
    void unfollow_whenTargetNotFound_throwsUserNotFoundException() {

        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(follower));
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> followService.unfollow(999L, "taro@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getFollowerCount_delegatesToRepository() {

        when(userFollowRepository.countByFollowingId(2L)).thenReturn(4L);

        assertThat(followService.getFollowerCount(2L)).isEqualTo(4L);
    }

    @Test
    void getFollowingCount_delegatesToRepository() {

        when(userFollowRepository.countByFollowerId(1L)).thenReturn(7L);

        assertThat(followService.getFollowingCount(1L)).isEqualTo(7L);
    }

    @Test
    void isFollowing_whenFollowerIdNull_returnsFalseWithoutQuery() {

        assertThat(followService.isFollowing(null, 2L)).isFalse();
    }

    @Test
    void isFollowing_delegatesToRepository() {

        when(userFollowRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);

        assertThat(followService.isFollowing(1L, 2L)).isTrue();
    }

    @Test
    void findFollowingIds_whenFollowerIdNull_returnsEmptySet() {

        assertThat(followService.findFollowingIds(null, List.of(2L, 3L))).isEqualTo(Set.of());
    }

    @Test
    void findFollowingIds_delegatesToRepository() {

        when(userFollowRepository.findFollowingIdsAmong(1L, List.of(2L, 3L))).thenReturn(List.of(2L));

        assertThat(followService.findFollowingIds(1L, List.of(2L, 3L))).isEqualTo(Set.of(2L));
    }
}
