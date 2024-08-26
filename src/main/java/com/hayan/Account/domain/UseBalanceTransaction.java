package com.hayan.Account.domain;

import com.hayan.Account.exception.CustomException;
import com.hayan.Account.exception.ErrorCode;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static lombok.AccessLevel.PROTECTED;

@Entity
@DiscriminatorValue("USE_BALANCE")
@NoArgsConstructor(access = PROTECTED)
public class UseBalanceTransaction extends Transaction {

    public UseBalanceTransaction(Account account, Integer amount, TransactionResult transactionResult) {
        super(account, amount, transactionResult);
    }

    @Override
    public void canCancel(int inputAmount, String inputAccountNumber) {
        if (!Objects.equals(this.getAmount(), inputAmount))
            throw new CustomException(ErrorCode.AMOUNT_MISMATCH);
        if (!Objects.equals(this.getAccount().getAccountNumber(), inputAccountNumber))
            throw new CustomException(ErrorCode.ACCOUNT_MISMATCH);
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
