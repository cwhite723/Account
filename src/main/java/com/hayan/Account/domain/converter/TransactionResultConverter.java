package com.hayan.Account.domain.converter;

import com.hayan.Account.domain.Transaction.TransactionResult;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TransactionResultConverter extends EnumConverter<TransactionResult> {
    public TransactionResultConverter() {
        super(TransactionResult.class);
    }
}
