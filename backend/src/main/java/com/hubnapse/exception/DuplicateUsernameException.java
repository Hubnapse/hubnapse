package com.hubnapse.exception;

public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException(String username) {
        super("このユーザー名は既に使用されています。username=" + username);
    }
}
