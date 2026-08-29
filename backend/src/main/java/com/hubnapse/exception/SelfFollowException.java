package com.hubnapse.exception;

public class SelfFollowException extends RuntimeException {

    public SelfFollowException() {
        super("自分自身をフォローすることはできません");
    }
}
