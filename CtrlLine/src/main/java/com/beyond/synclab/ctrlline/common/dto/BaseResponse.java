package com.beyond.synclab.ctrlline.common.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BaseResponse<T> {
    private final int code;
    private final T data;

    public static <T> BaseResponse<T> ok(T data) {
        return BaseResponse.<T>builder()
            .code(200)
            .data(data)
            .build();
    }

    public static <T> BaseResponse<T> of(int code, T data) {
        return BaseResponse.<T>builder()
            .code(code)
            .data(data)
            .build();
    }
}
