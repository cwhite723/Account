package com.hayan.Account.common;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationResponse<T> {
    private final boolean success;
    private final T data;

    public static <T> ApplicationResponse<T> ok(T data) {
        return new ApplicationResponse<>(true, data);
    }

    public static ApplicationResponse<Void> noData() {
        return new ApplicationResponse<>(true, null);
    }
}
