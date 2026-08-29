package com.hubnapse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hubnapse.entity.PostEntity;
import com.hubnapse.entity.PostLikeEntity;
import com.hubnapse.entity.UserEntity;
import com.hubnapse.exception.AlreadyLikedException;
import com.hubnapse.exception.PostNotFoundException;
import com.hubnapse.exception.UserNotFoundException;
import com.hubnapse.repository.PostLikeRepository;
import com.hubnapse.repository.PostRepository;
import com.hubnapse.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostLikeService postLikeService;

    private PostEntity post;
    private UserEntity user;

    @BeforeEach
    void setUp() {

        UserEntity author = new UserEntity();
        author.setId(1L);

        post = new PostEntity();
        post.setId(100L);
        post.setAuthor(author);

        user = new UserEntity();
        user.setId(2L);
        user.setEmail("jiro@example.com");
    }

    @Test
    void like_success() {

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail("jiro@example.com")).thenReturn(Optional.of(user));
        when(postLikeRepository.existsByPostIdAndUserId(100L, 2L)).thenReturn(false);

        postLikeService.like(100L, "jiro@example.com");

        verify(postLikeRepository).save(any(PostLikeEntity.class));
    }

    @Test
    void like_duplicate_throwsAlreadyLikedException() {

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail("jiro@example.com")).thenReturn(Optional.of(user));
        when(postLikeRepository.existsByPostIdAndUserId(100L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> postLikeService.like(100L, "jiro@example.com"))
                .isInstanceOf(AlreadyLikedException.class);

        verify(postLikeRepository, never()).save(any());
    }

    @Test
    void like_whenPostNotFound_throwsPostNotFoundException() {

        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postLikeService.like(999L, "jiro@example.com"))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void like_whenUserNotFound_throwsUserNotFoundException() {

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postLikeService.like(100L, "unknown@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void unlike_success() {

        when(postRepository.existsById(100L)).thenReturn(true);
        when(userRepository.findByEmail("jiro@example.com")).thenReturn(Optional.of(user));

        postLikeService.unlike(100L, "jiro@example.com");

        verify(postLikeRepository).deleteByPostIdAndUserId(100L, 2L);
    }

    @Test
    void unlike_whenNotLiked_isIdempotentAndDoesNotThrow() {

        when(postRepository.existsById(100L)).thenReturn(true);
        when(userRepository.findByEmail("jiro@example.com")).thenReturn(Optional.of(user));

        postLikeService.unlike(100L, "jiro@example.com");

        verify(postLikeRepository).deleteByPostIdAndUserId(100L, 2L);
    }

    @Test
    void unlike_whenPostNotFound_throwsPostNotFoundException() {

        when(postRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> postLikeService.unlike(999L, "jiro@example.com"))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void countByPostId_delegatesToRepository() {

        when(postLikeRepository.countByPostId(100L)).thenReturn(5L);

        assertThat(postLikeService.countByPostId(100L)).isEqualTo(5L);
    }

    @Test
    void isLikedBy_whenUserIdNull_returnsFalseWithoutQuery() {

        assertThat(postLikeService.isLikedBy(100L, null)).isFalse();
    }

    @Test
    void findLikedPostIds_whenUserIdNull_returnsEmptySet() {

        assertThat(postLikeService.findLikedPostIds(null, List.of(1L, 2L))).isEqualTo(Set.of());
    }

    @Test
    void countGroupedByPostIds_mapsProjectionToMap() {

        PostLikeRepository.PostLikeCount count1 = new PostLikeRepository.PostLikeCount() {
            @Override
            public Long getPostId() {
                return 100L;
            }

            @Override
            public Long getLikeCount() {
                return 3L;
            }
        };

        when(postLikeRepository.countGroupedByPostIds(List.of(100L))).thenReturn(List.of(count1));

        Map<Long, Long> result = postLikeService.countGroupedByPostIds(List.of(100L));

        assertThat(result).containsEntry(100L, 3L);
    }
}
