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

import com.hubnapse.dto.AiToolResponse;
import com.hubnapse.dto.UserAiToolsUpdateRequest;
import com.hubnapse.dto.UserProfileResponse;
import com.hubnapse.dto.UserProfileUpdateRequest;
import com.hubnapse.dto.UserRegisterRequest;
import com.hubnapse.dto.UserResponse;
import com.hubnapse.security.CurrentUser;
import com.hubnapse.service.AiToolService;
import com.hubnapse.service.FollowService;
import com.hubnapse.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FollowService followService;
    private final AiToolService aiToolService;

    @PostMapping
    public UserResponse register(
            @Valid @RequestBody UserRegisterRequest request) {

        return userService.register(request);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }

    @PutMapping("/me")
    public UserResponse updateProfile(
            @Valid @RequestBody UserProfileUpdateRequest request,
            Authentication authentication) {

        return userService.updateProfile(request, authentication.getName());
    }

    @PutMapping("/me/ai-tools")
    public List<AiToolResponse> updateAiTools(
            @Valid @RequestBody UserAiToolsUpdateRequest request,
            Authentication authentication) {

        return aiToolService.updateSelection(authentication.getName(), request.aiToolIds());
    }

    @GetMapping("/{username}")
    public UserProfileResponse getProfile(@PathVariable String username, Authentication authentication) {
        return userService.getProfileByUsername(username, CurrentUser.emailOrNull(authentication));
    }

    @PostMapping("/{userId}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void follow(@PathVariable Long userId, Authentication authentication) {
        followService.follow(userId, authentication.getName());
    }

    @DeleteMapping("/{userId}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(@PathVariable Long userId, Authentication authentication) {
        followService.unfollow(userId, authentication.getName());
    }
}
