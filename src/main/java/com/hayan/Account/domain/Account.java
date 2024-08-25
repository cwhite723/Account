package com.hayan.Account.domain;

import com.hayan.Account.common.BaseEntity;
import com.hayan.Account.domain.converter.AccountStatusConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public void close() {
        this.accountStatus = UNREGISTERED;
    }

    public void useBalance(Integer amount) {
        this.balance -= amount;
    }

    public void cancelUseBalance(Integer amount) {
        this.balance += amount;
    }
}
