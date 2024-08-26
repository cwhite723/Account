package com.hayan.Account.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.NoArgsConstructor;

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
    public String getType() {
        return "CANCEL_USE_BALANCE";
    }

    @Override
    public void process() {
        getAccount().deposit(getAmount());
    }
}
