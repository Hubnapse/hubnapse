package com.hubnapse.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hubnapse.dto.PostRequest;
import com.hubnapse.dto.PostResponse;
import com.hubnapse.security.CurrentUser;
import com.hubnapse.service.PostLikeService;
import com.hubnapse.service.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;

    @GetMapping
    public List<PostResponse> findAll(Authentication authentication) {
        return postService.findAll(CurrentUser.emailOrNull(authentication));
    }

    @GetMapping("/{id}")
    public PostResponse findById(@PathVariable Long id, Authentication authentication) {
        return postService.findById(id, CurrentUser.emailOrNull(authentication));
    }

    @PostMapping
    public PostResponse create(
            @Valid @RequestBody PostRequest request,
            Authentication authentication) {

        return postService.create(request, authentication.getName());
    }

    @PutMapping("/{id}")
    public PostResponse update(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request,
            Authentication authentication) {

        return postService.update(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        postService.delete(id, authentication.getName());
    }

    @PostMapping("/{postId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(@PathVariable Long postId, Authentication authentication) {
        postLikeService.like(postId, authentication.getName());
    }

    @DeleteMapping("/{postId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(@PathVariable Long postId, Authentication authentication) {
        postLikeService.unlike(postId, authentication.getName());
    }
}