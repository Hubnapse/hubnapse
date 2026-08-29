package com.hubnapse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
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
import org.springframework.security.access.AccessDeniedException;

import com.hubnapse.dto.PostRequest;
import com.hubnapse.dto.PostResponse;
import com.hubnapse.entity.PostEntity;
import com.hubnapse.entity.UserEntity;
import com.hubnapse.exception.PostNotFoundException;
import com.hubnapse.exception.UserNotFoundException;
import com.hubnapse.repository.PostRepository;
import com.hubnapse.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostLikeService postLikeService;

    @Mock
    private FollowService followService;

    @InjectMocks
    private PostService postService;

    private UserEntity owner;
    private UserEntity otherUser;
    private PostEntity postEntity;
    private PostRequest postRequest;

    @BeforeEach
    void setUp() {

        owner = new UserEntity();
        owner.setId(1L);
        owner.setUsername("taro");
        owner.setDisplayName("太郎");
        owner.setEmail("taro@example.com");
        owner.setIconUrl("https://example.com/taro.png");

        otherUser = new UserEntity();
        otherUser.setId(2L);
        otherUser.setUsername("jiro");
        otherUser.setDisplayName("次郎");
        otherUser.setEmail("jiro@example.com");

        postEntity = new PostEntity();
        postEntity.setId(10L);
        postEntity.setAuthor(owner);
        postEntity.setTitle("元のタイトル");
        postEntity.setDescription("元の説明");
        postEntity.setImageUrl("https://example.com/image.png");
        postEntity.setWhatCreated("イラスト");
        postEntity.setTips("元のtips");
        postEntity.setBestPrompt("元のprompt");
        postEntity.setCreatedAt(OffsetDateTime.now());
        postEntity.setUpdatedAt(OffsetDateTime.now());

        postRequest = new PostRequest(
                "新しいタイトル", "新しい説明", "https://example.com/new.png",
                "新しいイラスト", "新しいtips", "新しいprompt");
    }

    @Test
    void create_setsAuthenticatedUserAsAuthor() {

        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(owner));
        when(postRepository.save(any(PostEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostResponse response = postService.create(postRequest, "taro@example.com");

        assertThat(response.author().id()).isEqualTo(1L);
        assertThat(response.author().username()).isEqualTo("taro");
        assertThat(response.title()).isEqualTo("新しいタイトル");
        assertThat(response.likeCount()).isZero();
        assertThat(response.likedByCurrentUser()).isFalse();
        assertThat(response.author().followedByCurrentUser()).isFalse();
    }

    @Test
    void create_whenAuthorNotFound_throwsUserNotFoundException() {

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.create(postRequest, "unknown@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void update_byOwner_succeeds() {

        when(postRepository.findById(10L)).thenReturn(Optional.of(postEntity));
        when(postRepository.save(any(PostEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(postLikeService.countByPostId(10L)).thenReturn(3L);
        when(postLikeService.isLikedBy(10L, 1L)).thenReturn(true);

        PostResponse response = postService.update(10L, postRequest, "taro@example.com");

        assertThat(response.title()).isEqualTo("新しいタイトル");
        assertThat(response.author().username()).isEqualTo("taro");
        assertThat(response.likeCount()).isEqualTo(3L);
        assertThat(response.likedByCurrentUser()).isTrue();
        assertThat(response.author().followedByCurrentUser()).isFalse();
    }

    @Test
    void update_byNonOwner_throwsAccessDeniedException() {

        when(postRepository.findById(10L)).thenReturn(Optional.of(postEntity));

        assertThatThrownBy(() -> postService.update(10L, postRequest, "jiro@example.com"))
                .isInstanceOf(AccessDeniedException.class);

        verify(postRepository, never()).save(any());
    }

    @Test
    void update_whenPostNotFound_throwsPostNotFoundException() {

        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.update(99L, postRequest, "taro@example.com"))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void delete_byOwner_succeeds() {

        when(postRepository.findById(10L)).thenReturn(Optional.of(postEntity));

        postService.delete(10L, "taro@example.com");

        verify(postRepository).delete(postEntity);
    }

    @Test
    void delete_byNonOwner_throwsAccessDeniedException() {

        when(postRepository.findById(10L)).thenReturn(Optional.of(postEntity));

        assertThatThrownBy(() -> postService.delete(10L, "jiro@example.com"))
                .isInstanceOf(AccessDeniedException.class);

        verify(postRepository, never()).delete(any(PostEntity.class));
    }

    @Test
    void findAll_includesAuthorInResponse() {

        when(postRepository.findAll()).thenReturn(List.of(postEntity));

        List<PostResponse> responses = postService.findAll(null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).author().displayName()).isEqualTo("太郎");
        assertThat(responses.get(0).author().iconUrl()).isEqualTo("https://example.com/taro.png");
        assertThat(responses.get(0).likeCount()).isZero();
        assertThat(responses.get(0).likedByCurrentUser()).isFalse();
    }

    @Test
    void findAll_whenLoggedIn_includesLikeAndFollowStatusFromBatchLookups() {

        when(postRepository.findAll()).thenReturn(List.of(postEntity));
        when(userRepository.findIdByEmail("jiro@example.com")).thenReturn(Optional.of(2L));
        when(postLikeService.countGroupedByPostIds(List.of(10L))).thenReturn(Map.of(10L, 5L));
        when(postLikeService.findLikedPostIds(eq(2L), any())).thenReturn(Set.of(10L));
        when(followService.findFollowingIds(eq(2L), any())).thenReturn(Set.of(1L));

        List<PostResponse> responses = postService.findAll("jiro@example.com");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).likeCount()).isEqualTo(5L);
        assertThat(responses.get(0).likedByCurrentUser()).isTrue();
        assertThat(responses.get(0).author().followedByCurrentUser()).isTrue();
    }

    @Test
    void findById_includesAuthorInResponse() {

        when(postRepository.findById(10L)).thenReturn(Optional.of(postEntity));

        PostResponse response = postService.findById(10L, null);

        assertThat(response.author().username()).isEqualTo("taro");
        assertThat(response.likeCount()).isZero();
        assertThat(response.likedByCurrentUser()).isFalse();
    }

    @Test
    void findById_whenLoggedIn_includesLikeAndFollowStatus() {

        when(postRepository.findById(10L)).thenReturn(Optional.of(postEntity));
        when(userRepository.findIdByEmail("jiro@example.com")).thenReturn(Optional.of(2L));
        when(postLikeService.countByPostId(10L)).thenReturn(7L);
        when(postLikeService.isLikedBy(10L, 2L)).thenReturn(true);
        when(followService.isFollowing(2L, 1L)).thenReturn(true);

        PostResponse response = postService.findById(10L, "jiro@example.com");

        assertThat(response.likeCount()).isEqualTo(7L);
        assertThat(response.likedByCurrentUser()).isTrue();
        assertThat(response.author().followedByCurrentUser()).isTrue();
    }
}
