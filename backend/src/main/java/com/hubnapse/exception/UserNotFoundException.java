package com.hubnapse.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String email) {
        super("ユーザーが見つかりません。email=" + email);
    }
}
