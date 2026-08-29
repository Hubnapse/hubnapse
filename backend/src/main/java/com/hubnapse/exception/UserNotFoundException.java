package com.hubnapse.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String email) {
        super("ユーザーが見つかりません。email=" + email);
    }

    public UserNotFoundException(Long id) {
        super("ユーザーが見つかりません。id=" + id);
    }
}
