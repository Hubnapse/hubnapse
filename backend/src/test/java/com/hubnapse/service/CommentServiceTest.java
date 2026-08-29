package com.hubnapse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.springframework.security.access.AccessDeniedException;

import com.hubnapse.dto.CommentRequest;
import com.hubnapse.dto.CommentResponse;
import com.hubnapse.entity.CommentEntity;
import com.hubnapse.entity.PostEntity;
import com.hubnapse.entity.UserEntity;
import com.hubnapse.exception.CommentNotFoundException;
import com.hubnapse.exception.InvalidParentCommentException;
import com.hubnapse.exception.PostNotFoundException;
import com.hubnapse.exception.UserNotFoundException;
import com.hubnapse.repository.CommentRepository;
import com.hubnapse.repository.PostRepository;
import com.hubnapse.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowService followService;

    @InjectMocks
    private CommentService commentService;

    private UserEntity author;
    private UserEntity otherUser;
    private PostEntity post;
    private PostEntity otherPost;
    private CommentEntity topLevelComment;

    @BeforeEach
    void setUp() {

        author = new UserEntity();
        author.setId(1L);
        author.setUsername("taro");
        author.setDisplayName("太郎");
        author.setEmail("taro@example.com");

        otherUser = new UserEntity();
        otherUser.setId(2L);
        otherUser.setUsername("jiro");
        otherUser.setDisplayName("次郎");
        otherUser.setEmail("jiro@example.com");

        post = new PostEntity();
        post.setId(100L);
        post.setAuthor(author);

        otherPost = new PostEntity();
        otherPost.setId(200L);
        otherPost.setAuthor(author);

        topLevelComment = new CommentEntity();
        topLevelComment.setId(10L);
        topLevelComment.setPost(post);
        topLevelComment.setAuthor(author);
        topLevelComment.setParent(null);
        topLevelComment.setContent("最初のコメント");
        topLevelComment.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    void findByPostId_returnsCommentsWithAuthorAndParentId() {

        CommentEntity reply = new CommentEntity();
        reply.setId(11L);
        reply.setPost(post);
        reply.setAuthor(otherUser);
        reply.setParent(topLevelComment);
        reply.setContent("返信です");
        reply.setCreatedAt(OffsetDateTime.now());

        when(postRepository.existsById(100L)).thenReturn(true);
        when(commentRepository.findByPostIdOrderByCreatedAtAsc(100L))
                .thenReturn(List.of(topLevelComment, reply));

        List<CommentResponse> responses = commentService.findByPostId(100L, null);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).parentId()).isNull();
        assertThat(responses.get(0).author().username()).isEqualTo("taro");
        assertThat(responses.get(1).parentId()).isEqualTo(10L);
        assertThat(responses.get(1).author().username()).isEqualTo("jiro");
    }

    @Test
    void findByPostId_whenPostNotFound_throwsPostNotFoundException() {

        when(postRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> commentService.findByPostId(999L, null))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void create_topLevelComment_setsNullParent() {

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(author));
        when(commentRepository.save(any(CommentEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CommentRequest request = new CommentRequest("トップレベルコメント", null);

        CommentResponse response = commentService.create(100L, request, "taro@example.com");

        assertThat(response.parentId()).isNull();
        assertThat(response.content()).isEqualTo("トップレベルコメント");
        assertThat(response.author().username()).isEqualTo("taro");
    }

    @Test
    void create_reply_setsParentAndParentId() {

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail("jiro@example.com")).thenReturn(Optional.of(otherUser));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(topLevelComment));
        when(commentRepository.save(any(CommentEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CommentRequest request = new CommentRequest("返信コメント", 10L);

        CommentResponse response = commentService.create(100L, request, "jiro@example.com");

        assertThat(response.parentId()).isEqualTo(10L);
        assertThat(response.content()).isEqualTo("返信コメント");
    }

    @Test
    void create_whenParentBelongsToDifferentPost_throwsInvalidParentCommentException() {

        when(postRepository.findById(200L)).thenReturn(Optional.of(otherPost));
        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(author));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(topLevelComment));

        CommentRequest request = new CommentRequest("別投稿への返信", 10L);

        assertThatThrownBy(() -> commentService.create(200L, request, "taro@example.com"))
                .isInstanceOf(InvalidParentCommentException.class);

        verify(commentRepository, never()).save(any());
    }

    @Test
    void create_whenParentNotFound_throwsCommentNotFoundException() {

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail("taro@example.com")).thenReturn(Optional.of(author));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        CommentRequest request = new CommentRequest("存在しない親への返信", 999L);

        assertThatThrownBy(() -> commentService.create(100L, request, "taro@example.com"))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    void create_whenPostNotFound_throwsPostNotFoundException() {

        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        CommentRequest request = new CommentRequest("コメント", null);

        assertThatThrownBy(() -> commentService.create(999L, request, "taro@example.com"))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void create_whenAuthorNotFound_throwsUserNotFoundException() {

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        CommentRequest request = new CommentRequest("コメント", null);

        assertThatThrownBy(() -> commentService.create(100L, request, "unknown@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void delete_byOwner_succeeds() {

        when(commentRepository.findById(10L)).thenReturn(Optional.of(topLevelComment));

        commentService.delete(10L, "taro@example.com");

        verify(commentRepository).delete(topLevelComment);
    }

    @Test
    void delete_byNonOwner_throwsAccessDeniedException() {

        when(commentRepository.findById(10L)).thenReturn(Optional.of(topLevelComment));

        assertThatThrownBy(() -> commentService.delete(10L, "jiro@example.com"))
                .isInstanceOf(AccessDeniedException.class);

        verify(commentRepository, never()).delete(any(CommentEntity.class));
    }

    @Test
    void delete_whenCommentNotFound_throwsCommentNotFoundException() {

        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.delete(999L, "taro@example.com"))
                .isInstanceOf(CommentNotFoundException.class);
    }
}
