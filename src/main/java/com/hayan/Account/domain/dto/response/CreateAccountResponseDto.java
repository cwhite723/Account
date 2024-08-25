package com.hayan.Account.domain.dto.response;

import java.time.LocalDateTime;

public record CreateAccountResponseDto(
        String memberName,
        String accountNumber,
        LocalDateTime createTime
) {
    public static CreateAccountResponseDto of(String memberName, String accountNumber, LocalDateTime createTime) {
        return new CreateAccountResponseDto(memberName, accountNumber, createTime);
    }
}
