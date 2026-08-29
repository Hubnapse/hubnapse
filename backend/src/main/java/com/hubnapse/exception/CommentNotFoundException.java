package com.hubnapse.exception;

public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(Long id) {
        super("コメントが見つかりません。id=" + id);
    }
}
