package com.hayan.Account.domain;

import com.hayan.Account.exception.CustomException;
import com.hayan.Account.exception.ErrorCode;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static lombok.AccessLevel.PROTECTED;

@Entity
@DiscriminatorValue("CANCEL_USE_BALANCE")
@NoArgsConstructor(access = PROTECTED)
public class CancelUseTransaction extends Transaction {

    @OneToOne
    private Transaction originalTransaction;

    public CancelUseTransaction(Account account, Integer amount, Transaction originalTransaction, TransactionResult transactionResult) {
        super(account, amount, transactionResult);
        this.originalTransaction = originalTransaction;
    }

    @Override
    public void canCancel(int inputAmount, String inputAccountNumber) {
        throw new CustomException(ErrorCode.UNSUPPORTED_TRANSACTION_TYPE);
    }

    @Override
    public String getType() {
        return "CANCEL_USE_BALANCE";
    }

    @Override
    public void process() {
        getAccount().deposit(getAmount());
    }
}
