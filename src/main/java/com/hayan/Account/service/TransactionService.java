package com.hayan.Account.service;

import com.hayan.Account.domain.*;
import com.hayan.Account.domain.dto.request.CancelUseRequestDto;
import com.hayan.Account.domain.dto.request.UseBalanceRequestDto;
import com.hayan.Account.domain.dto.response.TransactionResponseDto;
import com.hayan.Account.exception.CustomException;
import com.hayan.Account.redis.DistributedLock;
import com.hayan.Account.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.hayan.Account.domain.Transaction.TransactionResult.FAILURE;
import static com.hayan.Account.domain.Transaction.TransactionResult.SUCCESS;
import static com.hayan.Account.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final MemberService memberService;
    private final AccountService accountService;
    private final AccountValidationService accountValidationService;
    private final TransactionRepository transactionRepository;

    @DistributedLock(key = "#request.accountNumber")
    public TransactionResponseDto useBalance(UseBalanceRequestDto request) {
        Member member = memberService.getByName(request.memberName());
        Account account = accountService.getByAccountNumber(request.accountNumber());

        accountValidationService.isAccountOwner(member.getId(), account.getMember().getId());

        Transaction transaction = null;

        try {
            withdraw(account, request.amount());
            transaction = new UseBalanceTransaction(account, request.amount(), SUCCESS);
        } catch (CustomException e) {
            transaction = new UseBalanceTransaction(account, request.amount(), FAILURE);
        } finally {
            transactionRepository.save(transaction);
        }

        return TransactionResponseDto.builder()
                .accountNumber(account.getAccountNumber())
                .transactionResult(transaction.getTransactionResult())
                .transactionId(transaction.getId())
                .amount(transaction.getAmount())
                .transactionDate(transaction.getCreatedAt())
                .build();
    }

    private void withdraw(Account account, int amount) {
        if (account.getAccountStatus().equals(Account.AccountStatus.UNREGISTERED))
            throw new CustomException(ACCOUNT_ALREADY_CLOSED);
        if (account.getBalance() < amount)
            throw new CustomException(INSUFFICIENT_BALANCE);

        account.withdraw(amount);
    }

    @DistributedLock(key = "#request.accountNumber")
    public TransactionResponseDto cancelUse(CancelUseRequestDto request) {
        Transaction originalTransaction = getById(request.transactionId());
        Account account = accountService.getByAccountNumber(originalTransaction.getAccount().getAccountNumber());

        validateCancelRequest(request, originalTransaction, account);

        Transaction transaction = null;

        try {
            account.deposit(request.amount());
            transaction = new CancelUseTransaction(account, request.amount(), originalTransaction, SUCCESS);
        } catch (CustomException e) {
            transaction = new CancelUseTransaction(account, request.amount(), originalTransaction, FAILURE);
        } finally {
            transactionRepository.save(transaction);
        }

        return TransactionResponseDto.builder()
                .accountNumber(account.getAccountNumber())
                .transactionResult(transaction.getTransactionResult())
                .transactionId(transaction.getId())
                .amount(transaction.getAmount())
                .transactionDate(transaction.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public TransactionResponseDto getTransaction(Long transactionId) {
        Transaction transaction = getById(transactionId);

        return TransactionResponseDto.builder()
                .accountNumber(transaction.getAccount().getAccountNumber())
                .transactionType(transaction.getType())
                .transactionResult(transaction.getTransactionResult())
                .transactionId(transactionId)
                .amount(transaction.getAmount())
                .transactionDate(transaction.getCreatedAt())
                .build();
    }

    public Transaction getById(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(TRANSACTION_NOT_FOUND));
    }

    private void validateCancelRequest(CancelUseRequestDto request, Transaction originalTransaction, Account account) {
        if (originalTransaction instanceof CancelUseTransaction)
            throw new CustomException(UNSUPPORTED_TRANSACTION_TYPE);

        if (transactionRepository.existsByOriginalTransaction(originalTransaction))
            throw new CustomException(TRANSACTION_ALREADY_CANCELLED);

        if (!request.amount().equals(originalTransaction.getAmount()))
            throw new CustomException(AMOUNT_MISMATCH);

        if (!request.accountNumber().equals(account.getAccountNumber()))
            throw new CustomException(ACCOUNT_MISMATCH);
    }
}
