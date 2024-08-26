package com.hayan.Account.domain;

import com.hayan.Account.common.BaseEntity;
import com.hayan.Account.domain.converter.AccountStatusConverter;
import com.hayan.Account.exception.CustomException;
import com.hayan.Account.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static com.hayan.Account.domain.Account.AccountStatus.IN_USE;
import static com.hayan.Account.domain.Account.AccountStatus.UNREGISTERED;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "accounts")
@NoArgsConstructor(access = PROTECTED)
public class Account extends BaseEntity {
    @Id
    @Column(name = "account_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    private Integer balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Convert(converter = AccountStatusConverter.class)
    private AccountStatus accountStatus;

    public enum AccountStatus {
        IN_USE,
        UNREGISTERED
    }

    private Account(Member member, String accountNumber, Integer balance) {
        this.member = member;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountStatus = IN_USE;
    }

    public static Account of(Member member, String accountNumber, Integer balance) {
        return new Account(member, accountNumber, balance);
    }

    public void close(Long loginId) {
        isOwner(loginId);
        if (this.balance > 0)
            throw new CustomException(ErrorCode.BALANCE_REMAINING_FOR_CLOSE);
        if (this.accountStatus == UNREGISTERED)
            throw new CustomException(ErrorCode.ACCOUNT_ALREADY_CLOSED);

        this.accountStatus = UNREGISTERED;
    }

    public void withdraw(Integer amount) {
        this.balance -= amount;
    }

    public void deposit(Integer amount) {
        this.balance += amount;
    }

    public void isOwner(Long loginId) {
        if (!Objects.equals(this.member.getId(), loginId)) {
            throw new CustomException(ErrorCode.USER_NOT_ACCOUNT_OWNER);
        }
    }
}
