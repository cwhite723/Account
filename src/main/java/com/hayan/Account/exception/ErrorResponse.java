package com.hayan.Account.exception;

public record ErrorResponse(
        Integer status,
        String name,
        String message
) {
    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getHttpStatus().value(), errorCode.name(), errorCode.getMessage());
    }
}
