package com.hayan.Account.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@DiscriminatorValue("USE_BALANCE")
@NoArgsConstructor(access = PROTECTED)
public class UseBalanceTransaction extends Transaction{

    public UseBalanceTransaction(Account account, Integer amount, TransactionResult transactionResult) {
        super(account, amount, transactionResult);
    }

    @Override
    public String getType() {
        return "USE_BALANCE";
    }

    @Override
    public void process() {
        getAccount().withdraw(getAmount());
    }
}
