package com.hayan.Account.domain;

import com.hayan.Account.common.BaseEntity;
import com.hayan.Account.domain.converter.TransactionResultConverter;
import com.hayan.Account.exception.CustomException;
import com.hayan.Account.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "transactions")
@NoArgsConstructor(access = PROTECTED)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "transaction_type")
public abstract class Transaction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private Integer amount;

    @Convert(converter = TransactionResultConverter.class)
    TransactionResult transactionResult;

    public enum TransactionResult {
        SUCCESS,
        FAILURE
    }

    public Transaction(Account account, Integer amount, TransactionResult transactionResult) {
        this.account = account;
        this.amount = amount;
        this.transactionResult = transactionResult;
    }

    public abstract void canCancel(int inputAmount, String inputAccountNumber);
    public abstract String getType();
    public abstract void process();
}
