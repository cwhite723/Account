package com.hayan.Account.domain.converter;

import com.hayan.Account.domain.Account.AccountStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AccountStatusConverter extends EnumConverter<AccountStatus> {
    public AccountStatusConverter() {
        super(AccountStatus.class);
    }
}
