package com.hubnapse.exception;

public class AlreadyLikedException extends RuntimeException {

    public AlreadyLikedException(Long postId) {
        super("この投稿には既にいいねしています。postId=" + postId);
    }
}
