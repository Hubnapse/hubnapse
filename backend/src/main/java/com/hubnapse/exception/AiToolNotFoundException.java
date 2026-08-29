package com.hubnapse.exception;

public class AiToolNotFoundException extends RuntimeException {

    public AiToolNotFoundException(Long id) {
        super("AIツールが見つかりません。id=" + id);
    }
}
