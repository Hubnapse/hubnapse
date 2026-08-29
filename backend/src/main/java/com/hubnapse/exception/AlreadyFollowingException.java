package com.hubnapse.exception;

public class AlreadyFollowingException extends RuntimeException {

    public AlreadyFollowingException(Long userId) {
        super("既にフォローしています。userId=" + userId);
    }
}
