package com.hayan.Account.service;

import com.hayan.Account.domain.Account;
import com.hayan.Account.exception.CustomException;
import com.hayan.Account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.hayan.Account.domain.Account.AccountStatus.UNREGISTERED;
import static com.hayan.Account.exception.ErrorCode.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountValidationService {
    private final AccountRepository accountRepository;

    void checkAccountCreationLimit(Long memberId) {
        if (accountRepository.countByMemberId(memberId) >= 10)
            throw new CustomException(ACCOUNT_LIMIT_EXCEEDED);
    }

    void isAccountOwner(Long loginMemberId, Long ownerMemberId) {
        if (!ownerMemberId.equals(loginMemberId))
            throw new CustomException(USER_NOT_ACCOUNT_OWNER);
    }

    void canCloseAccount(Account account) {
        hasRemainingBalance(account.getBalance());
        isClosed(account.getAccountStatus());
    }

    void canUseBalance(Account account, Integer amount) {
        isClosed(account.getAccountStatus());
        isBalanceInsufficient(account.getBalance(), amount);
    }

    private void hasRemainingBalance(Integer balance) {
        if (balance > 0)
            throw new CustomException(BALANCE_REMAINING_FOR_CLOSE);
    }

    private void isClosed(Account.AccountStatus status) {
        if (status == UNREGISTERED)
            throw new CustomException(ACCOUNT_ALREADY_CLOSED);
    }

    private void isBalanceInsufficient(Integer balance, Integer amount) {
        if (balance < amount)
            throw new CustomException(INSUFFICIENT_BALANCE);
    }
}
