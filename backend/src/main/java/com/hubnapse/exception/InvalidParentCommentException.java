package com.hubnapse.exception;

public class InvalidParentCommentException extends RuntimeException {

    public InvalidParentCommentException(Long parentId, Long postId) {
        super("返信先のコメントが指定された投稿に属していません。parentId=" + parentId + ", postId=" + postId);
    }
}
