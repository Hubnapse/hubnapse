package com.hubnapse.exception;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(Long id) {
        super("投稿が見つかりません。id=" + id);
    }
}
