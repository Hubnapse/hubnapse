package com.hubnapse.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hubnapse.dto.CommentRequest;
import com.hubnapse.dto.CommentResponse;
import com.hubnapse.security.CurrentUser;
import com.hubnapse.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/posts/{postId}/comments")
    public List<CommentResponse> findByPostId(@PathVariable Long postId, Authentication authentication) {
        return commentService.findByPostId(postId, CurrentUser.emailOrNull(authentication));
    }

    @PostMapping("/api/posts/{postId}/comments")
    public CommentResponse create(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {

        return commentService.create(postId, request, authentication.getName());
    }

    @DeleteMapping("/api/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long commentId, Authentication authentication) {
        commentService.delete(commentId, authentication.getName());
    }
}
