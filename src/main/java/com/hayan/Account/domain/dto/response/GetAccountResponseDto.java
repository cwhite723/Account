package com.hayan.Account.domain.dto.response;

import com.hayan.Account.domain.Account.AccountStatus;

public record GetAccountResponseDto(
        String accountNumber,
        Integer balance,
        AccountStatus accountStatus
) {
    public static GetAccountResponseDto of(String accountNumber, Integer balance, AccountStatus accountStatus) {
        return new GetAccountResponseDto(accountNumber, balance, accountStatus);
    }
}
