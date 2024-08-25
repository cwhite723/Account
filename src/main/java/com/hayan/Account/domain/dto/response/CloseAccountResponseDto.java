package com.hayan.Account.domain.dto.response;

import java.time.LocalDateTime;

public record CloseAccountResponseDto(
        String memberName,
        String accountNumber,
        LocalDateTime closeDate
) {
    public static CloseAccountResponseDto of(String memberName, String accountNumber, LocalDateTime closeDate) {
        return new CloseAccountResponseDto(memberName, accountNumber, closeDate);
    }
}
