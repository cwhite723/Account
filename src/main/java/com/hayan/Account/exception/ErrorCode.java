package com.hayan.Account.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum ErrorCode {
    // 400 Bad Request
    REQUEST_VALIDATION_FAIL(BAD_REQUEST, "잘못된 요청 값입니다."),
    INSUFFICIENT_BALANCE(BAD_REQUEST, "잔액이 부족합니다."),

    // 403 Forbidden
    USER_NOT_ACCOUNT_OWNER(FORBIDDEN, "해당 계좌의 소유주와 일치하지 않습니다."),

    // 404 Not Found
    ACCOUNT_NOT_FOUND(NOT_FOUND, "계좌 정보를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(NOT_FOUND, "사용자를 찾을 수 없습니다."),
    TRANSACTION_NOT_FOUND(NOT_FOUND, "거래를 찾을 수 없습니다."),

    // 409 Conflict
    ACCOUNT_LIMIT_EXCEEDED(CONFLICT, "계좌 개수 제한을 초과했습니다."),
    BALANCE_REMAINING_FOR_CLOSE(CONFLICT, "잔액이 남아 있어 계좌를 해지할 수 없습니다."),
    ACCOUNT_ALREADY_CLOSED(CONFLICT, "이미 해지된 계좌입니다."),
    LOCK_ACQUISITION_FAILED(CONFLICT, "잠금 획득에 실패했습니다."),
    TRANSACTION_FAILED(CONFLICT, "거래에 실패했습니다."),
    AMOUNT_MISMATCH(CONFLICT, "거래 금액이 일치하지 않습니다."),
    ACCOUNT_MISMATCH(CONFLICT, "계좌번호가 일치하지 않습니다."),
    UNSUPPORTED_TRANSACTION_TYPE(CONFLICT, "잔액 사용 거래가 아닙니다."),
    TRANSACTION_ALREADY_CANCELLED(CONFLICT, "이미 취소된 거래입니다."),

    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다. 관리자에게 문의하세요."),
    NO_AVAILABLE_PORT(HttpStatus.INTERNAL_SERVER_ERROR, "사용 가능한 포트가 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public String getName() {
        return name();
    }
}
